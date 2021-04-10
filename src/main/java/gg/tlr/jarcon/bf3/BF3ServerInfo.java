package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.Util;
import gg.tlr.jarcon.core.WordBuffer;
import gg.tlr.jarcon.frostbite.PingSite;
import gg.tlr.jarcon.frostbite.ServerInfo;
import gg.tlr.jarcon.frostbite.TeamScore;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

public record BF3ServerInfo(String name,
                            int currentPlayers,
                            int maxPlayers,
                            BF3Mode mode,
                            BF3Map map,
                            int roundsPlayed,
                            int roundsTotal,
                            List<TeamScore> scores,
                            String onlineState,
                            boolean ranked,
                            boolean punkBuster,
                            boolean hasPassword,
                            Duration uptime,
                            Duration roundTime,
                            InetSocketAddress address,
                            String punkBusterVersion,
                            boolean joinQueueEnabled,
                            String region,
                            PingSite closestPingSite,
                            String country,
                            boolean matchMakingEnabled) implements ServerInfo {

    public static BF3ServerInfo parse(String... data) {
        final WordBuffer buffer = new WordBuffer(data);

        return new BF3ServerInfo(
                buffer.read(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.read(BF3Mode::getById),
                buffer.read(BF3Map::getById),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readComplex(TeamScore::read),
                buffer.read(),
                buffer.readBool(),
                buffer.readBool(),
                buffer.readBool(),
                Duration.ofSeconds(buffer.readInt()),
                Duration.ofSeconds(buffer.readInt()),
                buffer.read(Util::parseIpPort),
                buffer.read(),
                buffer.readBool(),
                buffer.read(),
                PingSite.byId(buffer.read()),
                buffer.read(),
                buffer.readBool()
        );
    }
}
