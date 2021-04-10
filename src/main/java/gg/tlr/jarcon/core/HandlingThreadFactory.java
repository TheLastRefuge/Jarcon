package gg.tlr.jarcon.core;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.function.Supplier;

public class HandlingThreadFactory implements ThreadFactory {

    private final Function<Long, String> name;

    private volatile Supplier<Thread.UncaughtExceptionHandler> handler;

    private final Thread.UncaughtExceptionHandler proxyHandler = (t, e) -> {
        final Thread.UncaughtExceptionHandler h = handler.get();
        if (h != null) {
            try {
                h.uncaughtException(t, e);
            } catch (RuntimeException ex) {
                Jarcon.getLogger().error("HandlingThreadFactory handler failed", e);
            }
        } else Jarcon.getLogger().debug("HandlingThreadFactory supplied null handler: %s".formatted(handler));
    };

    public HandlingThreadFactory(Function<Long, String> name) {
        this.name = name;
    }

    public HandlingThreadFactory(Function<Long, String> name, Thread.UncaughtExceptionHandler handler) {
        this(name, () -> handler);
    }

    public HandlingThreadFactory(Function<Long, String> name, Supplier<Thread.UncaughtExceptionHandler> handler) {
        this(name);
        this.handler = handler;
    }

    public void setHandler(Supplier<Thread.UncaughtExceptionHandler> handler) {
        this.handler = handler;
    }

    public void setHandler(Thread.UncaughtExceptionHandler handler) {
        this.handler = () -> handler;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread thread = new Thread(r);

        try {
            thread.setName(name.apply(thread.getId()));
        } catch (RuntimeException e) {
            Jarcon.getLogger().error("HandlingThreadFactory failed to generate Thread name", e);
        }

        thread.setUncaughtExceptionHandler(proxyHandler);
        return thread;
    }
}
