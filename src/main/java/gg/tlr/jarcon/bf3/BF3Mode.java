package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.frostbite.GameMode;
import gg.tlr.jarcon.frostbite.Team;

public enum BF3Mode implements GameMode {

    CONQUEST("ConquestSmall0", "Conquest", true),
    RUSH("RushLarge0", "Rush", false) {
        @Override
        public Team getTeamById(int teamId) {
            return switch (teamId) {
                case 0 -> BF3Team.UNASSIGNED;
                case 1 -> BF3Team.Rush.ATTACKER;
                case 2 -> BF3Team.Rush.DEFENDER;
                default -> throw iae(teamId);
            };
        }
    },
    SQUAD_RUSH("SquadRush0", "Squad Rush", false) {
        @Override
        public Team getTeamById(int teamId) {
            return RUSH.getTeamById(teamId);
        }
    },
    SQUAD_DEATHMATCH("SquadDeathMatch0", "Squad Deathmatch", false) {
        @Override
        public Team getTeamById(int teamId) {
            return switch (teamId) {
                case 0 -> BF3Team.UNASSIGNED;
                case 1 -> BF3Team.SquadDeathmatch.ALPHA;
                case 2 -> BF3Team.SquadDeathmatch.BRAVO;
                case 3 -> BF3Team.SquadDeathmatch.CHARLIE;
                case 4 -> BF3Team.SquadDeathmatch.DELTA;
                default -> throw iae(teamId);
            };
        }
    },
    TEAM_DEATHMATCH("TeamDeathMatch0", "Team Deathmatch", true),
    CONQUEST_LARGE("ConquestLarge0", "Conquest Large", true),
    CONQUEST_ASSAULT_LARGE("ConquestAssaultLarge0", "Conquest Assault Large", true),
    CONQUEST_ASSAULT("ConquestAssaultSmall0", "Conquest Assault", true),
    GUN_MASTER("GunMaster0", "Gun Master", false),
    DOMINATION("Domination0", "Domination", true),
    TEAM_DM_CLOSE_QUARTERS("TeamDeathMatchC0", "Team DM Close Quarters", true),
    TANK_SUPERIORITY("TankSuperiority0", "Tank Superiority", true),
    CAPTURE_THE_FLAG("CaptureTheFlag0", "Capture the Flag", false),
    SCAVENGER("Scavenger0", "Scavenger", true),
    AIR_SUPERIORITY("AirSuperiority0", "Air Superiority", true);

    private final String  id;
    private final String  name;
    private final boolean normalTickets;

    BF3Mode(String id, String name, boolean normalTickets) {
        this.id = id;
        this.name = name;
        this.normalTickets = normalTickets;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasNormalTickets() {
        return normalTickets;
    }

    @Override
    public Team getTeamById(int teamId) {
        return switch (teamId) {
            case 0 -> BF3Team.UNASSIGNED;
            case 1 -> BF3Team.US;
            case 2 -> BF3Team.RU;
            default -> throw iae(teamId);
        };
    }

    public static BF3Mode getById(String id) {
        for (BF3Mode mode : values()) if (mode.getId().equals(id)) return mode;
        throw new IllegalArgumentException("Invalid GameMode ID: " + id);
    }

    IllegalArgumentException iae(int teamId) {
        return new IllegalArgumentException("Team ID %d invalid for Mode %s".formatted(teamId, this));
    }
}
