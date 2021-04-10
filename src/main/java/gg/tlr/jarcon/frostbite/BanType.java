package gg.tlr.jarcon.frostbite;

public enum BanType {
    PERMANENT("perm"),
    ROUNDS("rounds"),
    TIME("seconds");

    private final String id;

    BanType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static BanType getById(String id) {
        for (BanType value : values()) if (value.getId().equalsIgnoreCase(id)) return value;
        throw new IllegalArgumentException("Invalid BanType ID: " + id);
    }
}
