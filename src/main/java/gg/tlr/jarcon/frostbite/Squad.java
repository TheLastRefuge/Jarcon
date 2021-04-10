package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.Util;

public enum Squad {

    NONE,
    ALPHA,
    BRAVO,
    CHARLIE,
    DELTA,
    ECHO,
    FOXTROT,
    GOLF,
    HOTEL,
    INDIA,
    JULIET,
    KILO,
    LIMA,
    MIKE,
    NOVEMBER,
    OSCAR,
    PAPA,
    QUEBEC,
    ROME,
    SIERRA,
    TANGO,
    UNIFORM,
    VICTOR,
    WHISKEY,
    XRAY,
    YANKEE,
    ZULU,
    HAGGARD,
    SWEETWATER,
    PRESTON,
    REDFORD,
    FAITH,
    CELESTE;

    private final String name = Util.capitalize(name());

    public int getId() {
        return ordinal();
    }

    public String getName() {
        return name;
    }

    public static Squad getById(int id) {
        if (id < 0 || id > values().length) throw new IllegalArgumentException("Invalid Squad ID: " + id);
        return values()[id];
    }

    public static Squad getByName(String name) {
        for (Squad value : values()) if (value.getName().equalsIgnoreCase(name)) return value;
        throw new IllegalArgumentException("Invalid Squad name: " + name);
    }
}
