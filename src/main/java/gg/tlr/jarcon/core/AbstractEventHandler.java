package gg.tlr.jarcon.core;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("unused")
abstract class AbstractEventHandler<T> {

    private final ExecutorService executor  = Executors.newCachedThreadPool(new HandlingThreadFactory("Jarcon-Event-%d"::formatted, (t, e) -> Jarcon.getLogger().error("Unhandled Exception in EventHandler", e)));
    private final List<T>         listeners = new CopyOnWriteArrayList<>();

    public boolean registerListener(@Nonnull T listener) {
        return listeners.add(listener);
    }

    public boolean deregisterListener(@Nonnull T listener) {
        return listeners.remove(listener);
    }

    public void shutdown() {
        executor.shutdown();
    }

    protected void dispatch(Consumer<T> task) {
        for (T listener : listeners) executor.execute(() -> task.accept(listener));
    }

    protected abstract void handle(Packet packet);

    protected abstract void handle(JarconClient.Status previous, JarconClient.Status current);
}