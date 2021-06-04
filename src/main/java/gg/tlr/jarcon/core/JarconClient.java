package gg.tlr.jarcon.core;

import gg.tlr.jarcon.Util;
import gg.tlr.jarcon.frostbite.FrostbiteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public abstract class JarconClient implements AutoCloseable {

    private static final AtomicInteger ID = new AtomicInteger();

    private final SocketAddress    address;
    private final MetaHandler      metaHandler  = new MetaHandler();
    private final Queue<Action<?>> writeQueue   = new LinkedList<>();
    private final Thread           writeThread  = new WriteThread();
    private final Thread           shutdownHook = new Thread(() -> {
        if (getSettings().shutdownHook()) shutdown(false);
    });

    private final Map<String, RemoteError> errorRegistry = new ConcurrentHashMap<>();

    protected final Logger        logger = LoggerFactory.getLogger("JarconClient-%d".formatted(ID.getAndIncrement()));
    protected final ActionFactory action = new ActionFactory(this);

    @Nullable
    private volatile String           password;
    private volatile JarconConnection connection;
    private volatile ClientSettings   settings = new ClientSettings();
    private volatile Status           status   = Status.READY;

    public JarconClient(SocketAddress address) {
        this(address, null);
    }

    public JarconClient(SocketAddress address, @Nullable String password) {
        this.address = address;
        this.password = password;
        writeThread.start();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public Logger getLogger() {
        return logger;
    }

    public ActionFactory getActionFactory() {
        return action;
    }

    public ClientSettings getSettings() {
        return settings;
    }

    public void setSettings(@Nonnull ClientSettings settings) {
        this.settings = settings;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isConnected() {
        return status == Status.CONNECTED;
    }

    public boolean isLoggedIn() {
        return status == Status.LOGGED_IN;
    }

    @Override
    public void close() {
        shutdown(false);
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                synchronized (JarconClient.this) {
                    doConnect();
                    if (password != null) doLogin(password);
                    if (settings.eventsEnabled() && this instanceof FrostbiteClient client) {
                        client.eventsEnabled(true).complete();
                    }
                }
                return null;
            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    public CompletableFuture<Void> login() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                synchronized (JarconClient.this) {
                    doLogin(password);
                }
                return null;
            } catch (ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    public void shutdown(boolean now) {
        try {
            doShutdown(now);
        } catch (InterruptedException e) {
            logger.warn("Shutdown interrupted", e);
        }
    }

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    public abstract EventHandler<?> getEventHandler();

    protected abstract boolean compatibleWith(@Nonnull Version version);

    public <T extends Enum<?> & RemoteError> void registerErrors(Class<T> clazz) {
        final T[] errors = clazz.getEnumConstants();

        for (T error : errors) {
            final RemoteError previous = errorRegistry.putIfAbsent(error.getId(), error);
            if(previous != null) {
                throw new IllegalArgumentException("%s was already registered by '%s'".formatted(error.getId(), previous.getProviderName()));
            }
        }
    }

    public <T extends Enum<?> & RemoteError> void deregisterErrors(Class<T> clazz) {
        errorRegistry.values().removeIf(error -> error.getClass() == clazz);
    }

    //region Commands
    //----------------------------------------------------------------------------------------------------

    @CheckReturnValue
    private Action<String> loginHashed() {
        return action.new String(false, "login.hashed");
    }

    @CheckReturnValue
    private Action<Void> loginHashed(String passwordHash) {
        return action.new Void(false, "login.hashed", passwordHash);
    }

    @Deprecated //Vulnerable to MITM
    @CheckReturnValue
    private Action<Void> loginPlaintext(String password) {
        logger.warn("Logging in with plaintext password (insecure!)");
        return action.new Void(false, "login.plainText", password);
    }

    @CheckReturnValue
    public Action<Version> version() {
        return action.new Packet(false, "version").map(packet -> {
            WordBuffer buffer = new WordBuffer(packet.data());
            return new Version(buffer.read(), buffer.read());
        });
    }

    @CheckReturnValue
    public Action<List<String>> help() {
        return action.new Packet(true, "admin.help").map(packet -> Arrays.asList(packet.data()));
    }

    @CheckReturnValue
    private Action<Void> logout() {
        return action.new Void(false, "logout");
    }

    @CheckReturnValue
    private Action<Void> quit() {
        return action.new Void(false, "quit");
    }

    @CheckReturnValue
    public <T> Action<T> getVar(@Nonnull Var<T> var) {
        return action.new Packet(true, var.getName()).map(packet -> var.parse(packet.word()));
    }

    @CheckReturnValue
    public <T> Action<Void> setVar(@Nonnull Var<T> var, T value) {
        if (var.isReadOnly()) throw new IllegalArgumentException("Var is read-only");
        if (var.isClamped()) logger.warn("Discarding return value of clamped var");

        var.validate(value);
        return action.new Void(true, var.getName(), value);
    }

    @CheckReturnValue
    public <T> Action<T> setVarClamped(Var<T> var, T value) {
        if (!var.isClamped()) throw new IllegalArgumentException("Var is not clamped");
        if (var.isReadOnly()) throw new IllegalArgumentException("Var is read-only");
        var.validate(value);
        return action.new Packet(true, var.getName(), value).map(packet -> var.parse(packet.word()));
    }

    //----------------------------------------------------------------------------------------------------
    //endregion

    public void queue(Action<?> action) {
        if (status == Status.SHUTDOWN) action.getFuture().completeExceptionally(new RuntimeException("Client shut down"));
        if (action.isSecure() && status != Status.LOGGED_IN && password == null) logger.warn("Secure action queued without password");
        else {
            synchronized (writeQueue) {
                writeQueue.add(action);
                writeQueue.notifyAll();
            }
        }
    }

    private synchronized void setStatus(Status status) {
        if (this.status == Status.SHUTDOWN) return;
        getEventHandler().handle(this.status, status);
        logger.debug("Status set to %s".formatted(status));
        this.status = status;
    }

    private void reconnect() {
        logger.info("Client reconnecting...");

        setStatus(Status.RECONNECTING);

        while (settings.autoReconnect()) {
            long delay = settings.reconnectDelay();

            synchronized (this) {
                if (status == Status.SHUTDOWN || status == Status.LOGGED_IN) return;

                try {
                    doConnect();
                } catch (Exception e) {
                    logger.error("Failed to connect", e);
                    delay = settings.reconnectDelay();
                }

                try {
                    if (status == Status.CONNECTED && password != null) doLogin(password);
                } catch (Exception e) {
                    logger.error("Failed to login", e);

                    final Throwable cause = e.getCause();
                    if (cause instanceof ErrorResponseException ere) {
                        switch (ere.getFrostbiteError()) {
                            case INVALID_PASSWORD_HASH, INVALID_PASSWORD, PASSWORD_NOT_SET -> delay = settings.reconnectLoginDelay();
                        }
                    }
                }

                try {
                    if(settings.eventsEnabled() && this instanceof FrostbiteClient client) {
                        client.eventsEnabled(true).complete();
                    }
                } catch(Exception e) {
                    logger.error("Failed to enable events", e);
                }
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.debug("Reconnect interrupted", e);
            }
        }
    }

    private synchronized void doConnect() throws IOException, ExecutionException, InterruptedException {
        switch (status) {
            case SHUTDOWN -> throw new IllegalStateException("Client shut down");
            case READY, DISCONNECTED, RECONNECTING -> {
                logger.info("Client connecting...");

                connection = new JarconConnection(this, address);
                connection.connect();
                connection.getDeathFuture().exceptionally(throwable -> {
                    setStatus(Status.DISCONNECTED);
                    if (settings.autoReconnect()) new Thread(this::reconnect).start();
                    return null;
                });

                final Version version = version().complete();
                if (!compatibleWith(version)) throw new RuntimeException("Incompatible version: %s".formatted(version));
                logger.info("Client connected to version %s".formatted(version));

                setStatus(Status.CONNECTED);
            }
        }
    }

    private synchronized void doLogin(String password) throws ExecutionException, InterruptedException {
        if (status == Status.LOGGED_IN) return;
        if (password == null) throw new IllegalStateException("No password set"); //Technically IAE but the parameter is always state
        if (status != Status.CONNECTED) throw new IllegalStateException("Client not connected");

        final String salt = loginHashed().complete();

        try {
            loginHashed(Util.hashPassword(password, salt)).complete();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            loginPlaintext(password).complete();
        }

        setStatus(Status.LOGGED_IN);
        synchronized (writeQueue) {
            writeQueue.notifyAll();
        }
    }

    private synchronized void doShutdown(boolean now) throws InterruptedException {
        if (status == Status.SHUTDOWN) return;
        try {
            if (Thread.currentThread() != shutdownHook) Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
        }

        setStatus(Status.SHUTDOWN);
        logger.info("Client shutting down...");

        if (now) {
            final RuntimeException e = new RuntimeException("Client shut down");
            writeThread.interrupt();
            synchronized (writeQueue) {
                logger.debug("Client discarding %d packets".formatted(writeQueue.size()));
                writeQueue.forEach(action -> action.getFuture().completeExceptionally(e));
                writeQueue.clear();
            }
        } else {
            synchronized (writeQueue) {
                logger.debug("Client waiting to send %d packets".formatted(writeQueue.size()));
                while (!writeQueue.isEmpty()) writeQueue.wait();
            }
            writeThread.interrupt();
        }

        writeThread.join();

        try {
            if (connection != null) connection.shutdown(now);
        } catch (RuntimeException e) {
            logger.error("Unhandled Exception encountered while shutting down connection", e);
        }

        try {
            getMetaHandler().shutdown();
        } catch (RuntimeException e) {
            logger.error("Unhandled Exception encountered while shutting down meta handler", e);
        }

        logger.info("Client shutdown complete");
    }

    private Action<?> dequeue() throws InterruptedException {
        synchronized (writeQueue) {
            while (true) {
                if (status == Status.LOGGED_IN) {
                    final Action<?> action = writeQueue.poll();
                    if (action != null) {
                        writeQueue.notifyAll();
                        return action;
                    }
                } else {
                    //TODO Find more efficient solution
                    for (Action<?> action : writeQueue) {
                        if (!action.isSecure()) {
                            writeQueue.remove(action);
                            writeQueue.notifyAll();
                            return action;
                        }
                    }
                }

                writeQueue.wait();
            }
        }
    }

    private class WriteThread extends Thread {
        private final Queue<Action<?>> pending = new LinkedList<>();

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    if (!pending.isEmpty() && isLoggedIn()) {
                        @SuppressWarnings("ConstantConditions")
                        final boolean handled = sendRetrying(pending.peek());
                        if (handled) pending.remove();
                    } else {
                        final Action<?> action = dequeue();
                        final boolean handled = sendRetrying(action);
                        if (!handled) pending.add(action); //Action is secure and we were logged out
                    }
                }
                logger.debug("WriteThread interrupted");
            } catch (InterruptedException e) {
                logger.debug("WriteThread interrupted", e);
            } catch (RuntimeException e) {
                logger.error("WriteThread terminated unexpectedly", e);
            }
        }

        private boolean sendRetrying(Action<?> action) throws InterruptedException {
            boolean handled;
            do {
                if (action.isSecure() && !isLoggedIn()) return false;

                handled = connection != null && connection.send(action);
                if (!handled) Thread.sleep(settings.retryDelay());
            } while (!handled);

            return true;
        }
    }

    public enum Status {
        READY,
        CONNECTED,
        LOGGED_IN,
        DISCONNECTED,
        RECONNECTING,
        SHUTDOWN
    }
}