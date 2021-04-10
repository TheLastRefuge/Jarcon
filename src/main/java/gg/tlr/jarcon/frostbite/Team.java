package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.Util;

public interface Team {
    int getId();

    String name();

    default String getName() {
        return Util.capitalize(name());
    }

    Team UNASSIGNED = new Team() {
        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String name() {
            return "UNASSIGNED";
        }
    };
}
