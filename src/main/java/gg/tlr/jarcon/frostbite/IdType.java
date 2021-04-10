package gg.tlr.jarcon.frostbite;

public enum IdType {
    NAME,
    IP,
    GUID;

    public String getId() {
        return name().toLowerCase();
    }
}
