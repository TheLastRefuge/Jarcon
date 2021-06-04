package gg.tlr.jarcon.core;

import java.util.concurrent.TimeUnit;

public class ClientSettings {

    private volatile boolean shutdownHook         = true;
    private volatile boolean autoReconnect        = true;
    private volatile boolean logAllErrorResponses = true;
    private volatile boolean eventsEnabled        = false;

    //In milliseconds
    private volatile long    retryDelay           = 250;
    private volatile long    reconnectDelay       = TimeUnit.SECONDS.toMillis(5);
    private volatile long    reconnectLoginDelay  = TimeUnit.SECONDS.toMillis(10);
    private volatile long    shutdownTimeout      = TimeUnit.SECONDS.toMillis(10);
    private volatile long    defaultActionTimeout = TimeUnit.SECONDS.toMillis(15);

    public boolean shutdownHook() {
        return shutdownHook;
    }

    public void shutdownHook(boolean shutdownHook) {
        this.shutdownHook = shutdownHook;
    }

    public boolean autoReconnect() {
        return autoReconnect;
    }

    public void autoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean logAllErrorResponses() {
        return logAllErrorResponses;
    }

    public void logAllErrorResponses(boolean logAllErrorResponses) {
        this.logAllErrorResponses = logAllErrorResponses;
    }

    public boolean eventsEnabled() {
        return eventsEnabled;
    }

    public void eventsEnabled(boolean enabled) {
        this.eventsEnabled = enabled;
    }

    public long reconnectDelay() {
        return reconnectDelay;
    }

    public void reconnectDelay(long reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public long reconnectLoginDelay() {
        return reconnectLoginDelay;
    }

    public void reconnectLoginDelay(long reconnectLoginDelay) {
        this.reconnectLoginDelay = reconnectLoginDelay;
    }

    public long shutdownTimeout() {
        return shutdownTimeout;
    }

    public void shutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public long retryDelay() {
        return retryDelay;
    }

    public void retryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public long defaultActionTimeout() {
        return defaultActionTimeout;
    }

    public void defaultActionTimeout(long actionTimeout) {
        this.defaultActionTimeout = actionTimeout;
    }
}
