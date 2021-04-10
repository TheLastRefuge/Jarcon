package gg.tlr.jarcon.frostbite;

public enum FrostbiteVersion {
    UNKNOWN,
    BF3;

    public static FrostbiteVersion getById(String id) {
        for (FrostbiteVersion value : values()) {
            if (value.name().equalsIgnoreCase(id)) return value;
        }
        return UNKNOWN;
    }
}
