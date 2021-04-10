package gg.tlr.jarcon.frostbite;

import java.time.Duration;

public record Timeout(Object...words) {

    private static final Timeout PERM = new Timeout(BanType.PERMANENT.getId());

    public static Timeout permanent() {
        return PERM;
    }

    public static Timeout rounds(int rounds) {
        return new Timeout(BanType.ROUNDS.getId(), rounds);
    }

    public static Timeout duration(Duration duration) {
        return new Timeout(BanType.TIME.getId(), duration.toSeconds());
    }
}
