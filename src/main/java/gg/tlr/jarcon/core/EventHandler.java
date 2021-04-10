package gg.tlr.jarcon.core;

public abstract class EventHandler<T extends JarconListener> extends AbstractEventHandler<T> {

    protected void handle(JarconClient.Status previous, JarconClient.Status current) {
        dispatch(listeners -> listeners.onJarconStatus(previous, current));
    }
}
