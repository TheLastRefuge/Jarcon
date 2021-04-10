package gg.tlr.jarcon.frostbite;

public interface GameMap {
    String getId();

    String getName();

    record Indices(int current, int next) {
    }

    record Rounds(int played, int total) {
    }
}
