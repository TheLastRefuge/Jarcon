package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.WordBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TeamScore(int teamId,
                        float tickets,
                        int target) {

    public static List<TeamScore> read(WordBuffer buffer) {
        final List<TeamScore> list = new ArrayList<>();
        final int entries = buffer.readInt();
        final int target = buffer.readInt(buffer.position() + entries);

        for (int i = 0; i < entries; i++) {
            list.add(new TeamScore(i + 1, buffer.readFloat(), target));
        }

        //Skip target
        buffer.jump(1);

        return Collections.unmodifiableList(list);
    }
}
