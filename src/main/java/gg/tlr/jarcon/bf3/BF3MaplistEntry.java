package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.core.WordBuffer;
import gg.tlr.jarcon.frostbite.MaplistEntry;

public record BF3MaplistEntry(BF3Map map,
                              BF3Mode mode,
                              int rounds) implements MaplistEntry {

    public static final int PARAMETER_COUNT = 3;

    public static BF3MaplistEntry read(WordBuffer buffer) {
        return new BF3MaplistEntry(
                buffer.read(BF3Map::getById),
                buffer.read(BF3Mode::getById),
                buffer.readInt()
        );
    }
}
