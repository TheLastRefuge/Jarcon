package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.frostbite.GameMap;

@SuppressWarnings("SpellCheckingInspection")
public enum BF3Map implements GameMap {

    GRAND_BAZAAR("MP_001", "Grand Bazaar"),
    TEHRAN_HIGHWAY("MP_003", "Tehran Highway"),
    CASPIAN_BORDER("MP_007", "Caspian Border"),
    SEINE_CROSSING("MP_011", "Seine Crossing"),
    OPERATION_FIRESTORM("MP_012", "Operation Firestorm"),
    DAMAVAND_PEAK("MP_013", "Damavand Peak"),
    NOSHAHR_CANALS("MP_017", "Noshahr Canals"),
    KHARG_ISLAND("MP_018", "Kharg Island"),
    OPERATION_METRO("MP_Subway", "Operation Métro"),

    STRIKE_AT_KARKAND("XP1_001", "Strike at Karkand"),
    GULF_OF_OMAN("XP1_002", "Gulf of Oman"),
    SHARQI_PENINSULA("XP1_003", "Sharqi Peninsula"),
    WAKE_ISLAND("XP1_004", "Wake Island"),

    SCRAPMETAL("XP2_Factory", "Scrapmetal"),
    OPERATION_925("XP2_Office", "Operation 925"),
    DONYA_FORTRESS("XP2_Palace", "Donya Fortress"),
    ZIBA_TOWER("XP2_Skybar", "Ziba Tower"),

    BANDAR_DESERT("XP3_Desert", "Bandar Desert"),
    ALBORZ_MOUNTAINS("XP3_Alborz", "Alborz Mountains"),
    ARMORED_SHIELD("XP3_Shield", "Armored Shield"),
    DEATH_VALLEY("XP3_Valley", "Death Valley"),

    MARKAZ_MONOLITH("XP4_FD", "Markaz Monolith"),
    AZADI_PALACE("XP4_Parl", "Azadi Palace"),
    EPICENTER("XP4_Quake", "Epicenter"),
    TALAH_MARKET("XP4_Rubble", "Talah Market"),

    OPERATION_RIVERSIDE("XP5_001", "Operation Riverside"),
    NEBANDAN_FLATS("XP5_002", "Nebandan Flats"),
    KIASAR_RAILROAD("XP5_003", "Kiasar Railroad"),
    SABALAN_PIPELINE("XP5_004", "Sabalan Pipeline");


    private final String id, name;

    BF3Map(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public static BF3Map getById(String id) {
        for (BF3Map map : values()) if (map.getId().equals(id)) return map;
        throw new IllegalArgumentException("Invalid Map ID: " + id);
    }

}
