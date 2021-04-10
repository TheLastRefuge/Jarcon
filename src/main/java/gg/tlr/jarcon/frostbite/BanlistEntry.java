package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.WordBuffer;

public record BanlistEntry(IdType idType,
                           String id,
                           BanType banType,
                           int seconds,
                           int rounds,
                           String reason) {

    public static final int PARAMETER_COUNT = 6;

    public static BanlistEntry read(WordBuffer buffer) {
        return new BanlistEntry(
                buffer.readEnum(IdType.class),
                buffer.read(),
                buffer.read(BanType::getById),
                buffer.readInt(),
                buffer.readInt(),
                buffer.read()
        );
    }
}
