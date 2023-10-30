package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.WordBuffer;

public sealed interface Subset permits Subset.All, Subset.Team, Subset.Squad, Subset.Player {

    Type getType();

    String[] serialize();

    static Subset all() {
        return All.INSTANCE;
    }

    static Team team(gg.tlr.jarcon.frostbite.Team team) {
        return new Team(team.getId());
    }

    static Squad squad(gg.tlr.jarcon.frostbite.Team team, gg.tlr.jarcon.frostbite.Squad squad) {
        return new Squad(team.getId(), squad);
    }

    static Player player(String name) {
        return new Player(name);
    }

    static Subset read(WordBuffer buffer) {
        final Type type = Type.getById(buffer.read());
        return switch (type) {
            case ALL -> All.INSTANCE;
            case TEAM -> new Team(buffer.readInt());
            case SQUAD -> new Squad(buffer.readInt(), gg.tlr.jarcon.frostbite.Squad.getById(buffer.readInt()));
            case PLAYER -> new Player(buffer.read());
        };
    }

    record All() implements Subset {
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

    record Team(int team) implements Subset {

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

    record Squad(int team, gg.tlr.jarcon.frostbite.Squad squad) implements Subset {

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

    record Player(String name) implements Subset {

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

    enum Type {
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
