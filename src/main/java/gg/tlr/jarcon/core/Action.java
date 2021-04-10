package gg.tlr.jarcon.core;

import gg.tlr.jarcon.Util;

import javax.annotation.CheckReturnValue;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public abstract class Action<T> {

    private static final Consumer<Throwable> DEFAULT_HANDLER = t -> Jarcon.getLogger().error("Action Exception", t);

    private final JarconClient client;
    private final String[]     words;
    private final boolean      secure;

    protected volatile CompletableFuture<T> future;
    protected volatile long                 timeout;
    protected volatile TimeoutMode          mode    = TimeoutMode.STRICT;
    protected volatile boolean              sent;

    public Action(JarconClient client, boolean secure, Object... words) {
        this(client, secure, Util.flattenStringArray(words));
    }

    public Action(JarconClient client, boolean secure, String... words) {
        this.client = client;
        this.secure = secure;
        this.words = words;
        this.timeout = client.getSettings().defaultActionTimeout();
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isSent() {
        return sent;
    }

    @CheckReturnValue
    public Action<T> timeout(long value, TimeUnit unit) {
        checkQueued();
        timeout = unit.toMillis(value);
        return this;
    }

    @CheckReturnValue
    public Action<T> mode(TimeoutMode mode) {
        checkQueued();
        this.mode = mode;
        return this;
    }

    public <O> Action<O> map(Function<T, O> function) {
        checkQueued();
        return new MapAction<>(this, function);
    }

    public CompletableFuture<T> queue() {
        return queue(client.getSettings().logAllErrorResponses() ? DEFAULT_HANDLER : t -> {});
    }

    public T complete() throws ExecutionException, InterruptedException {
        return queue().get();
    }

    public CompletableFuture<T> queue(Consumer<Throwable> error) {
        return queue(s -> {}, error);
    }

    public CompletableFuture<T> queue(Consumer<T> success, Consumer<Throwable> error) {
        checkQueued();

        //Safe mode actions must not time out when sent
        if (mode == TimeoutMode.SAFE) {
            this.future = new CompletableFuture<>();
            new CompletableFuture<>().orTimeout(timeout, TimeUnit.MILLISECONDS).exceptionally(throwable -> {
                if (!sent) future.completeExceptionally(throwable);
                return null;
            });
        } else this.future = new CompletableFuture<T>().orTimeout(timeout, TimeUnit.MILLISECONDS);

        this.future.whenComplete((s, t) -> {
            try {
                if (t != null) error.accept(t);
                else success.accept(s);
            } catch (RuntimeException e) {
                DEFAULT_HANDLER.accept(new RuntimeException("Exception in consumer", e));
            }
        });

        client.queue(this);
        return future;
    }

    protected abstract T interpret(Packet packet);

    CompletableFuture<T> getFuture() {
        return future;
    }

    String[] getWords() {
        return words;
    }

    void complete(Packet packet) {
        try {
            future.complete(interpret(packet));
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to interpret response", e));
        }
    }

    void setSent() {
        this.sent = true;
    }

    private void checkQueued() {
        if (future != null) throw new IllegalStateException("Action already queued");
    }

    @Override
    public String toString() {
        return "Action{" +
                "client=" + client +
                ", words=" + Arrays.toString(words) +
                ", secure=" + secure +
                ", future=" + future +
                ", timeout=" + timeout +
                ", mode=" + mode +
                ", sent=" + sent +
                '}';
    }

    private static class MapAction<I, O> extends Action<O> {
        private final Function<Packet, O> function;

        public MapAction(Action<I> action, Function<I, O> function) {
            super(action.client, action.secure, action.words);
            this.function = packet -> function.apply(action.interpret(packet));

            //Copy fields
            this.timeout = action.timeout;
            this.mode = action.mode;
            this.sent = action.sent;
        }

        @Override
        protected O interpret(Packet packet) {
            return function.apply(packet);
        }
    }

    public enum TimeoutMode {
        STRICT,
        SAFE
    }
}
