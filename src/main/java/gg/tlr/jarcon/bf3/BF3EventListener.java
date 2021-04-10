package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.core.JarconListener;
import gg.tlr.jarcon.frostbite.PlayerInfo;
import gg.tlr.jarcon.frostbite.Squad;
import gg.tlr.jarcon.frostbite.Subset;
import gg.tlr.jarcon.frostbite.TeamScore;

import java.util.List;

@SuppressWarnings("unused")
public interface BF3EventListener extends JarconListener {

    default void onAuthenticated(String name) { }

    default void onJoin(String name, String guid) { }

    default void onLeave(String name, PlayerInfo info) { }

    default void onSpawn(String name, int teamId) { }

    default void onKill(String killer, String victim, String weapon, boolean headShot) { }

    default void onChat(String source, String message, Subset subset) { }

    default void onSquadChange(String name, int teamId, Squad squad) { }

    default void onTeamChange(String name, int teamId, Squad squad) { }

    default void onPunkBusterMessage(String message) { }

    default void onMaxPlayerCountChange(int maxPlayerCount) { }

    default void onLevelLoaded(BF3Map map, BF3Mode mode, BF3Map.Rounds rounds) { }

    default void onRoundOver(int winningTeamId) { }

    default void onRoundOverPlayers(List<PlayerInfo> info) { }

    default void onRoundOverTeamScores(List<TeamScore> scores) { }
}
