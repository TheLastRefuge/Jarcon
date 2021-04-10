package gg.tlr.jarcon.frostbite;

public interface GameMode {
    String getId();

    String getName();

    boolean hasNormalTickets();

    Team getTeamById(int teamId);
}
