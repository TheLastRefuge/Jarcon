package gg.tlr.jarcon.core;

public abstract class EventHandler<T extends JarconListener> extends AbstractEventHandler<T> {

    protected void handle(JarconClient.State previous, JarconClient.State current) {
        dispatch(listeners -> listeners.onJarconStatus(previous, current));
    }
}
