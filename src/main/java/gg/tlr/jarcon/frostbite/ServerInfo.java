package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.Country;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

public interface ServerInfo {
    String name();
    int currentPlayers();
    int maxPlayers();
    GameMode mode();
    GameMap map();
    int roundsPlayed();
    int roundsTotal();
    List<TeamScore> scores();
    String onlineState();
    boolean ranked();
    boolean punkBuster();
    boolean hasPassword();
    Duration uptime();
    Duration roundTime();
    InetSocketAddress address();
    String punkBusterVersion();
    boolean joinQueueEnabled();
    String region();
    PingSite closestPingSite();
    Country country();
    boolean matchMakingEnabled();
}
