package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.WordBuffer;

public abstract class Subset {

    public abstract Type getType();

    public abstract String[] serialize();

    public static Subset all() {
        return All.INSTANCE;
    }

    public static Team team(gg.tlr.jarcon.frostbite.Team team) {
        return new Team(team.getId());
    }

    public static Squad squad(gg.tlr.jarcon.frostbite.Team team, gg.tlr.jarcon.frostbite.Squad squad) {
        return new Squad(team.getId(), squad);
    }

    public static Player player(String name) {
        return new Player(name);
    }

    public static Subset read(WordBuffer buffer) {
        final Type type = Type.getById(buffer.read());
        return switch (type) {
            case ALL -> All.INSTANCE;
            case TEAM -> new Team(buffer.readInt());
            case SQUAD -> new Squad(buffer.readInt(), gg.tlr.jarcon.frostbite.Squad.getById(buffer.readInt()));
            case PLAYER -> new Player(buffer.read());
        };
    }

    public static final class All extends Subset {
        private static final All INSTANCE = new All();

        @Override
        public String[] serialize() {
            return new String[]{"all"};
        }

        @Override
        public Type getType() {
            return Type.ALL;
        }
    }

    public static final class Team extends Subset {
        private final int team;

        private Team(int team) {
            this.team = team;
        }

        public int getTeam() {
            return team;
        }

        @Override
        public Type getType() {
            return Type.TEAM;
        }

        @Override
        public String[] serialize() {
            return new String[]{"team", Integer.toString(team)};
        }
    }

    public static final class Squad extends Subset {
        private final int                           team;
        private final gg.tlr.jarcon.frostbite.Squad squad;

        private Squad(int team, gg.tlr.jarcon.frostbite.Squad squad) {
            this.team = team;
            this.squad = squad;
        }

        public int getTeam() {
            return team;
        }

        public gg.tlr.jarcon.frostbite.Squad getSquad() {
            return squad;
        }

        @Override
        public Type getType() {
            return Type.SQUAD;
        }

        @Override
        public String[] serialize() {
            return new String[]{"squad", Integer.toString(team), Integer.toString(squad.getId())};
        }
    }

    public static final class Player extends Subset {
        private final String name;

        private Player(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Type getType() {
            return Type.PLAYER;
        }

        @Override
        public String[] serialize() {
            return new String[]{"player", name};
        }
    }

    public enum Type {
        ALL,
        TEAM,
        SQUAD,
        PLAYER;

        public static Type getById(String id) {
            for (Type value : values()) if (value.name().equalsIgnoreCase(id)) return value;
            throw new IllegalArgumentException("Invalid Subset ID: " + id);
        }
    }
}
