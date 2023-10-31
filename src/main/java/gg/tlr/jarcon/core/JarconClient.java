package gg.tlr.jarcon.core;

import gg.tlr.jarcon.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class JarconClient implements AutoCloseable {

    private static final AtomicInteger ID = new AtomicInteger();

    private final SocketAddress    address;
    private final int              id           = ID.getAndIncrement();
    private final MetaHandler      metaHandler  = new MetaHandler();
    private final Queue<Action<?>> writeQueue   = new LinkedList<>();
    private final WriteThread      writeThread  = new WriteThread(id);
    private final Thread           shutdownHook = new Thread(() -> {
        if (getSettings().shutdownHook()) close(getSettings().awaitQuiescence());
    });

    private final Map<String, RemoteError> errorRegistry = new ConcurrentHashMap<>();

    protected final Logger                   logger       = LoggerFactory.getLogger("JarconClient-%d".formatted(id));
    protected final ActionFactory            action       = new ActionFactory(this);
    protected final JarconClientStateMachine stateMachine = new JarconClientStateMachine();

    @Nullable
    private volatile String           password;
    private volatile JarconConnection connection;
    private volatile ClientSettings   settings = new ClientSettings();
    private volatile boolean          closed;

    public JarconClient(SocketAddress address) {
        this(address, null);
    }

    public JarconClient(SocketAddress address, @Nullable String password) {
        this.address = address;
        this.password = password;
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        this.connection = new JarconConnection(this, address);
        writeThread.start();
    }

    public int getId() {
        return id;
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

    public State getState() {
        return stateMachine.get();
    }

    public boolean isConnected() {
        var state = getState();
        return state == State.CONNECTED || state == State.AUTHENTICATED;
    }

    public boolean isAuthenticated() {
        return getState() == State.AUTHENTICATED;
    }

    @Override
    public void close() {
        try {
            doClose(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(getSettings().awaitQuiescence()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(long awaitQuiescence) {
        if (closed) return;
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(awaitQuiescence);

        try {
            while (System.nanoTime() < deadline && !closed) {
                synchronized (writeQueue) {
                    if (writeQueue.isEmpty()) break;
                    writeQueue.wait(Math.max(0, System.nanoTime() - deadline));
                }
            }

            if (!closed) doClose(deadline);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public CompletableFuture<Void> connect() {
        synchronized (stateMachine) {
            if (stateMachine.set(State.CONNECTING)) return stateMachine.getFuture(State.CONNECTED);
            else throw new IllegalStateException("Can't connect from state %s".formatted(getState()));
        }
    }

    public CompletableFuture<Void> login() {
        synchronized (stateMachine) {
            if (stateMachine.set(State.AUTHENTICATING)) return stateMachine.getFuture(State.AUTHENTICATED);
            else throw new IllegalStateException("Can't authenticate from state %s".formatted(getState()));
        }
    }

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    public abstract EventHandler<?> getEventHandler();

    protected abstract boolean compatibleWith(@Nonnull Version version);

    protected abstract void report(ErrorResponseException e);

    public <T extends Enum<?> & RemoteError> void registerErrors(Class<T> clazz) {
        final T[] errors = clazz.getEnumConstants();

        for (T error : errors) {
            final RemoteError previous = errorRegistry.putIfAbsent(error.getId(), error);
            if (previous != null) {
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
        if (closed) action.getFuture().completeExceptionally(new RuntimeException("Client closed"));

        if (action.isSecure() && getState() != State.AUTHENTICATED && password == null) logger.warn("Secure action queued without password");

        synchronized (writeQueue) {
            writeQueue.add(action);
            writeQueue.notifyAll();
        }
    }

    private synchronized void doClose(long deadline) throws InterruptedException {
        if (closed) return;
        logger.info("Client closing...");

        if (Thread.currentThread() != shutdownHook) Runtime.getRuntime().removeShutdownHook(shutdownHook);

        closed = true;

        stateMachine.set(State.DISCONNECTED);

        writeThread.interrupt();
        writeThread.join();

        try {
            if (connection != null) connection.shutdown(deadline);
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

    private Action<?> findNextUnsecure() {
        synchronized (writeQueue) {
            return writeQueue.stream().filter(Predicate.not(Action::isSecure)).findFirst().orElse(null);
        }
    }

    private class WriteThread extends Thread {

        public WriteThread(int id) {
            setName("WriteThread-%d".formatted(id));
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {

                    //Wait until queue no longer empty
                    while (true) {
                        synchronized (writeQueue) {
                            if (writeQueue.isEmpty()) writeQueue.wait();
                            else break;
                        }
                    }

                    switch (JarconClient.this.getState()) {
                        case AUTHENTICATED -> {
                            Action<?> action;
                            synchronized (writeQueue) {
                                action = writeQueue.peek();
                            }

                            if (action == null) continue;

                            if (connection.send(action)) {
                                synchronized (writeQueue) {
                                    writeQueue.remove(action);
                                    writeQueue.notifyAll();
                                }
                            } else {
                                //Connection died, wait for new one
                                Thread.sleep(settings.retryDelay());
                            }
                        }
                        case CONNECTING, CONNECTED, AUTHENTICATING -> {
                            var action = findNextUnsecure();
                            if (action == null) {
                                synchronized (writeQueue) {
                                    writeQueue.wait();
                                }
                                continue;
                            }

                            JarconClient.State state = null;
                            //As long as not interrupted and enum cases still valid, try to send this action
                            while (!Thread.interrupted() &&
                                    (state = JarconClient.this.getState()) == JarconClient.State.CONNECTING
                                    || state == JarconClient.State.CONNECTED
                                    || state == JarconClient.State.AUTHENTICATING) {

                                if (connection.send(action)) {
                                    synchronized (writeQueue) {
                                        writeQueue.remove(action);
                                        writeQueue.notifyAll();
                                    }

                                    break;
                                } else {
                                    //Connection died, wait for new one
                                    Thread.sleep(getSettings().retryDelay());
                                }
                            }
                        }
                        default -> {
                            try {
                                stateMachine.getFuture(JarconClient.State.CONNECTING).get();
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                logger.debug("WriteThread interrupted");
            } catch (InterruptedException e) {
                logger.debug("WriteThread interrupted");
            } catch (RuntimeException e) {
                logger.error("WriteThread terminated unexpectedly", e);
                close();
            }
        }
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        AUTHENTICATING,
        AUTHENTICATED,
    }

    public class JarconClientStateMachine extends StateMachine<State> {
        private final CompletableFuture<Void>[] futures = new CompletableFuture[size];
        private       Thread                    taskExecutor;

        public JarconClientStateMachine() {
            super(State.class, State.DISCONNECTED);

            for (int i = 0; i < size; i++) {
                futures[i] = new CompletableFuture<>();
            }

            //Connect success
            add(State.CONNECTING, State.CONNECTED);

            //Login success
            add(State.AUTHENTICATING, State.AUTHENTICATED);

            //Login
            add(State.CONNECTED, State.AUTHENTICATING, this::createAuthenticationTask);

            //Logout
            add(State.AUTHENTICATED, State.AUTHENTICATING, this::createAuthenticationTask);

            //Disconnect
            for (State value : State.values()) {
                if (value == State.DISCONNECTED) continue;

                add(value, State.DISCONNECTED, () -> {
                    taskExecutor.interrupt();
                    try {
                        taskExecutor.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    synchronized (writeQueue) {
                        logger.debug("Client discarding %d packets".formatted(writeQueue.size()));
                        writeQueue.forEach(action -> action.getFuture().completeExceptionally(new RuntimeException("Client disconnected")));
                        writeQueue.clear();
                    }
                });
            }

            //Connect
            for (State value : State.values()) {

                add(value, State.CONNECTING, this::createConnectingTask);
            }
        }

        private void createTask(Runnable runnable) {
            taskExecutor = new Thread(runnable);
            taskExecutor.start();
        }

        @Override
        public synchronized boolean set(State target) {
            var previous = state;
            if (super.set(target)) {
                logger.debug("Setting state from %s to %s".formatted(previous, target));

                futures[target.ordinal()].complete(null);
                futures[target.ordinal()] = new CompletableFuture<>();

                return true;
            }

            return false;
        }

        public synchronized CompletableFuture<Void> getFuture(State state) {
            return futures[state.ordinal()];
        }

        private void createAuthenticationTask() {
            createTask(() -> {
                try {
                    while (!Thread.interrupted()) {
                        try {
                            var pw = password;
                            while ((pw = password) == null) {
                                logger.warn("Attempting to authenticate without password");
                                Thread.sleep(2000);
                            }

                            final String salt = loginHashed().complete();

                            try {
                                loginHashed(Util.hashPassword(pw, salt)).complete();
                            } catch (NoSuchAlgorithmException e) {
                                logger.error("Failed to hash password", e);
                                loginPlaintext(password).complete();
                            }

                            set(State.AUTHENTICATED);

                            synchronized (writeQueue) {
                                writeQueue.notifyAll();
                            }

                            return;
                        } catch (ExecutionException e) {
                            logger.error("Failed to authenticate", e);
                        }
                    }
                } catch (InterruptedException e) {

                }
            });
        }

        private void createConnectingTask() {
            createTask(() -> {
                try {
                    while (!Thread.interrupted()) {
                        try {
                            logger.info("Client connecting to %s...".formatted(address));
                            connection = new JarconConnection(JarconClient.this, address);
                            connection.connect();

                            final Version version = version().complete();
                            if (!compatibleWith(version)) {
                                logger.error("Incompatible version: %s".formatted(version));

                                /*
                                Drop connection if server has wrong version, but keep connecting in case it changes.
                                This might be undesirable as it drops unsecure packets that could have waited longer,
                                though it's unlikely packets are queued with such a long timeout.
                                Alternatively filter only login-relevant packets in WriteThread when connecting.
                                */
                                connection.shutdown(0);
                                Thread.sleep(getSettings().reconnectDelay());
                                continue;
                            }

                            logger.info("Client connected to version %s".formatted(version));
                            set(State.CONNECTED);

                            connection.getDeathFuture().exceptionally(throwable -> {
                                set(State.CONNECTING);
                                return null;
                            });

                            synchronized (writeQueue) {
                                writeQueue.notifyAll();
                            }

                            if (getSettings().autoLogin()) set(State.AUTHENTICATING);
                            return;
                        } catch (Exception e) {
                            logger.error("Failed to connect", e);
                            Thread.sleep(getSettings().reconnectDelay());
                        }
                    }
                } catch (InterruptedException e) {

                }
            });
        }
    }
}