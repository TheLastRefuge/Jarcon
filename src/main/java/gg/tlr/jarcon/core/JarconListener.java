package gg.tlr.jarcon.core;

public interface JarconListener {
    default void onJarconStatus(JarconClient.State previous, JarconClient.State current) { }
}
