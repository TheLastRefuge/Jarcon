package gg.tlr.jarcon.frostbite;

public enum Region {
    NORTH_AMERICA("NAm"),
    SOUTH_AMERICA("SAm"),
    ANTARCTICA("AC"),
    AFRICA("Afr"),
    EUROPE("EU"),
    ASIA("Asia"),
    OCEANIA("OC");

    private final String id;

    Region(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Region getById(String id) {
        for (Region region : values()) if (region.getId().equals(id)) return region;
        throw new IllegalArgumentException("Invalid Region ID: " + id);
    }
}
