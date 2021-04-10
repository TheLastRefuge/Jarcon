package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.frostbite.Team;

public enum BF3Team implements Team {

    US {
        @Override
        public int getId() {
            return 1;
        }
    },
    RU {
        @Override
        public int getId() {
            return 2;
        }
    };

    public enum Rush implements Team {
        ATTACKER {
            @Override
            public int getId() {
                return 1;
            }
        },
        DEFENDER {
            @Override
            public int getId() {
                return 2;
            }
        }
    }

    public enum SquadDeathmatch implements Team {
        ALPHA {
            @Override
            public int getId() {
                return 1;
            }
        },
        BRAVO {
            @Override
            public int getId() {
                return 2;
            }
        },
        CHARLIE {
            @Override
            public int getId() {
                return 3;
            }
        },
        DELTA {
            @Override
            public int getId() {
                return 4;
            }
        }
    }
}
