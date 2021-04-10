package gg.tlr.jarcon.core;

public interface JarconListener {
    default void onJarconStatus(JarconClient.Status previous, JarconClient.Status current) { }
}
