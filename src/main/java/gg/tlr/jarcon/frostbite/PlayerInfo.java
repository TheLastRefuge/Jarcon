package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.Jarcon;
import gg.tlr.jarcon.core.WordBuffer;

import java.util.ArrayList;
import java.util.List;

public record PlayerInfo(String name,
                         String guid,
                         int teamId,
                         Squad squad,
                         int kills,
                         int deaths,
                         int score,
                         int rank) {

    public static final int PARAMETER_COUNT = 8;

    public static PlayerInfo read(WordBuffer buffer) {
        return new PlayerInfo(
                buffer.read(),
                buffer.read(),
                buffer.readInt(),
                Squad.getById(buffer.readInt()),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt()
        );
    }

    public static List<PlayerInfo> readBlock(WordBuffer buffer) {
        final List<PlayerInfo> list = new ArrayList<>();
        final int paramCount = buffer.readInt();
        final int extraParams = paramCount - PARAMETER_COUNT;
        buffer.jump(paramCount); //Skip parameter names
        final int playerCount = buffer.readInt();

        if (extraParams > 0) Jarcon.getLogger().warn("Received %d extra parameter%s in Player info block".formatted(extraParams, extraParams == 1 ? "" : "s"));

        for (int i = 0; i < playerCount; i++) {
            list.add(buffer.readComplex(PlayerInfo::read));
            buffer.jump(extraParams);
        }

        return list;
    }
}
