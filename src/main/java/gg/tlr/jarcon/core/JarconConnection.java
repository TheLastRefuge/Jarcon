package gg.tlr.jarcon.core;

import gg.tlr.jarcon.Util;
import gg.tlr.jarcon.frostbite.FrostbiteError;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JarconConnection {

    private static final String OK_RESPONSE = "OK";
    private static final int MAX_PACKET_SIZE = 16384;

    private final JarconClient            client;
    private final SocketAddress           address;
    private final Logger                  logger;
    private final ExecutorService         executor;
    private final Thread                  readThread;
    private final Socket                  socket         = new Socket();
    private final AtomicInteger           sequenceNumber = new AtomicInteger();
    private final Map<Integer, Action<?>> transactions   = Collections.synchronizedMap(new HashMap<>());
    private final CompletableFuture<Void> deathFuture    = new CompletableFuture<>();

    private volatile boolean shutdown;

    JarconConnection(JarconClient client, SocketAddress address) {
        this.client = client;
        this.address = address;
        this.logger = client.getLogger();
        this.executor = Executors.newCachedThreadPool(new HandlingThreadFactory("Jarcon-Dispatch-%d"::formatted, (t, e) -> logger.error("Unhandled Exception", e)));
        this.readThread = new ReadThread(client.getId());
    }

    public CompletableFuture<Void> getDeathFuture() {
        return deathFuture;
    }

    public synchronized void connect() throws IOException {
        if (shutdown) throw new IllegalStateException("Connection shut down");

        socket.connect(address);
        readThread.start();
    }

    public synchronized void shutdown(long deadline) throws InterruptedException {
        logger.debug("Connection shutting down...");
        shutdown = true;

        synchronized (transactions) {
            logger.debug("Connection awaiting %d transactions".formatted(transactions.size()));
            while (System.nanoTime() < deadline && !transactions.isEmpty()) {
                transactions.wait(Math.max(0, System.nanoTime() - deadline));
            }

            readThread.interrupt();
            executor.shutdown();
            logger.debug("Connection awaiting scheduled tasks");
            if(!executor.awaitTermination(Math.max(0, System.nanoTime() - deadline), TimeUnit.NANOSECONDS)) executor.shutdownNow();

            if(!transactions.isEmpty()) {
                logger.debug("Connection discarding %d transactions".formatted(transactions.size()));
                cancelTransactions(new RuntimeException("Connection shut down"));
            }
        }

        try {
            socket.close();
        } catch (IOException ignored) {
        }

        readThread.join();
        logger.debug("Connection shut down");
    }

    private void cancelTransactions(RuntimeException e) {
        synchronized (transactions) {
            transactions.forEach((integer, action) -> action.getFuture().completeExceptionally(e));
            transactions.clear();
            transactions.notifyAll();
        }
    }

    private void die(Throwable throwable) {
        shutdown = true;
        logger.warn("Connection died", throwable);

        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Failed to close socket", e);
        }

        readThread.interrupt();
        cancelTransactions(new RuntimeException("Connection died"));
        executor.shutdown();
        deathFuture.completeExceptionally(throwable);
    }

    public synchronized boolean send(Action<?> action) {
        if (action.getFuture().isDone()) return true;
        if (shutdown || !socket.isConnected()) return false;

        action.setSent();

        try {
            final int sequence = sequenceNumber.getAndIncrement();
            transactions.put(sequence, action);

            final Packet packet = Packet.create(sequence, action.getWords());
            logger.trace("Sending packet: %s".formatted(packet));

            final byte[] raw = packet.encode();
            if (raw.length <= MAX_PACKET_SIZE) {
                logger.trace("Writing packet: %n%s".formatted(Util.generateHexDump(raw)));
                socket.getOutputStream().write(raw);
            } else {
                action.getFuture().completeExceptionally(new IllegalArgumentException("Packet too large"));
            }

            return true;
        } catch (IOException e) {
            die(e);
            return false;
        }
    }

    private void receive(Packet packet) {
        if (packet.words().length == 0) throw new RuntimeException("Empty response (Protocol violation)");

        if (packet.serverPacket() && !packet.response()) {
            logger.trace("Receiving event: %s".formatted(packet));
            executor.execute(() -> client.getMetaHandler().handle(packet));

        } else if (!packet.serverPacket() && packet.response()) {
            logger.trace("Receiving response: %s".formatted(packet));

            final String status = packet.status();
            final Action<?> action = transactions.remove(packet.sequence());

            if (action != null) {
                if (status.equals(OK_RESPONSE)) executor.execute(() -> action.complete(packet));
                else {
                    final ErrorResponseException e = createErrorResponse(status);
                    if(e.getFrostbiteError() == FrostbiteError.LOGIN_REQUIRED) logger.error("Secure Action sent unauthenticated: " + action);
                    action.getFuture().completeExceptionally(e);
                    client.report(e);
                }

                //Notification delayed to submit task before executor is shutdown
                synchronized (transactions) {
                    transactions.notifyAll();
                }
            } else logger.error("Unknown sequence number: %d".formatted(packet.sequence()));

        } else logger.error("Unhandled packet: %s".formatted(packet));
    }

    private ErrorResponseException createErrorResponse(String error) {
        final RemoteError remoteError = Optional.<RemoteError>ofNullable(FrostbiteError.getById(error)).orElse(client.lookupError(error));
        if(remoteError != null) return new ErrorResponseException(remoteError);
        else return new ErrorResponseException(error);
    }

    private class ReadThread extends Thread {

        public ReadThread(int id) {
            setName("ReadThread-%d".formatted(id));
        }

        @Override
        public void run() {
            try {
                final InputStream inputStream = socket.getInputStream();

                while (!Thread.interrupted()) {
                    final byte[] header = inputStream.readNBytes(Packet.HEADER_SIZE);
                    final ByteBuffer buffer = ByteBuffer.wrap(header);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);

                    if (header.length == 0) continue;
                    final int size = Util.unsignedToSigned(buffer.getInt(4));
                    final byte[] packet = new byte[size];
                    final int remaining = size - Packet.HEADER_SIZE;

                    System.arraycopy(header, 0, packet, 0, Packet.HEADER_SIZE);
                    System.arraycopy(inputStream.readNBytes(remaining), 0, packet, Packet.HEADER_SIZE, remaining);
                    logger.trace("Reading packet: %n%s".formatted(Util.generateHexDump(packet)));
                    receive(Packet.decode(packet));
                }

                logger.debug("ReadThread interrupted");

            } catch (IOException e) {
                if (socket.isClosed()) {
                    logger.debug("ReadThread terminated gracefully");
                } else {
                    logger.debug("ReadThread terminated ungracefully", e);
                    die(e);
                }

            } catch (RuntimeException e) {
                logger.error("ReadThread terminated unexpectedly", e);
                die(e);
            }
        }
    }
}
