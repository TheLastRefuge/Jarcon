package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.core.EventHandler;
import gg.tlr.jarcon.core.Packet;
import gg.tlr.jarcon.core.WordBuffer;
import gg.tlr.jarcon.frostbite.PlayerInfo;
import gg.tlr.jarcon.frostbite.Squad;
import gg.tlr.jarcon.frostbite.Subset;
import gg.tlr.jarcon.frostbite.TeamScore;

import java.util.List;

public class BF3EventHandler extends EventHandler<BF3EventListener> {

    @Override
    public void handle(Packet packet) {
        final WordBuffer buffer = new WordBuffer(packet.words());

        switch (buffer.read()) {
            case "player.onAuthenticated" -> {
                final String name = buffer.read();

                dispatch(listener -> listener.onAuthenticated(name));
            }
            case "player.onJoin" -> {
                final String name = buffer.read();
                final String guid = buffer.read();

                dispatch(listener -> listener.onJoin(name, guid));
            }
            case "player.onLeave" -> {
                final String name = buffer.read();
                final PlayerInfo info = PlayerInfo.readBlock(buffer).get(0);

                dispatch(listener -> listener.onLeave(name, info));
            }
            case "player.onSpawn" -> {
                final String name = buffer.read();
                final int teamId = buffer.readInt();

                dispatch(listener -> listener.onSpawn(name, teamId));
            }
            case "player.onKill" -> {
                final String killer = buffer.read();
                final String victim = buffer.read();
                final String weapon = buffer.read();
                final boolean headShot = buffer.readBool();

                dispatch(listener -> listener.onKill(killer, victim, weapon, headShot));
            }
            case "player.onChat" -> {
                final String source = buffer.read();
                final String message = buffer.read();
                final Subset subset = Subset.read(buffer);

                dispatch(listener -> listener.onChat(source, message, subset));
            }
            case "player.onSquadChange" -> {
                final String name = buffer.read();
                final int teamId = buffer.readInt();
                final Squad squad = Squad.getById(buffer.readInt());

                dispatch(listener -> listener.onSquadChange(name, teamId, squad));
            }
            case "player.onTeamChange" -> {
                final String name = buffer.read();
                final int teamId = buffer.readInt();
                final Squad squad = Squad.getById(buffer.readInt());

                dispatch(listener -> listener.onTeamChange(name, teamId, squad));
            }
            case "punkBuster.onMessage" -> {
                final String message = buffer.read();

                dispatch(listener -> listener.onPunkBusterMessage(message));
            }
            case "server.onMaxPlayerCountChange" -> {
                final int maxPlayerCount = buffer.readInt();

                dispatch(listener -> listener.onMaxPlayerCountChange(maxPlayerCount));
            }
            case "server.onLevelLoaded" -> {
                final BF3Map map = BF3Map.getById(buffer.read());
                final BF3Mode mode = BF3Mode.getById(buffer.read());
                final BF3Map.Rounds rounds = new BF3Map.Rounds(buffer.readInt(), buffer.readInt());

                dispatch(listener -> listener.onLevelLoaded(map, mode, rounds));
            }
            case "server.onRoundOver" -> {
                final int winningTeamId = buffer.readInt();

                dispatch(listener -> listener.onRoundOver(winningTeamId));
            }
            case "server.onRoundOverPlayers" -> {
                final List<PlayerInfo> playerInfo = PlayerInfo.readBlock(buffer);

                dispatch(listener -> listener.onRoundOverPlayers(playerInfo));
            }
            case "server.onRoundOverTeamScores" -> {
                final List<TeamScore> scores = TeamScore.read(buffer);

                dispatch(listener -> listener.onRoundOverTeamScores(scores));
            }
        }
    }
}
