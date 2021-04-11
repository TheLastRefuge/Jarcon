package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.bf3.BF3MaplistEntry;
import gg.tlr.jarcon.core.Action;
import gg.tlr.jarcon.core.JarconClient;
import gg.tlr.jarcon.core.WordBuffer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class FrostbiteClient extends JarconClient {
    public static final Predicate<String> PASSWORD_PREDICATE = Pattern.compile("[a-zA-Z0-9]{0,16}").asMatchPredicate();

    public FrostbiteClient(SocketAddress address) {
        this(address, null);
    }

    public FrostbiteClient(SocketAddress address, @Nullable String password) {
        super(address, password);
    }

    @CheckReturnValue
    public abstract Action<? extends ServerInfo> serverInfo();

    @CheckReturnValue
    public Action<Collection<PlayerInfo>> listPlayers(Subset subset) {
        return action.new Packet(false, isLoggedIn() ? "admin.listPlayers" : "listPlayers", subset.serialize())
                .map(packet -> new WordBuffer(packet.data()).readComplex(PlayerInfo::readBlock));
    }

    @CheckReturnValue
    public Action<Void> eventsEnabled(boolean enabled) {
        return action.new Void(true, "admin.eventsEnabled", enabled);
    }

    //TODO Change client password?
    @CheckReturnValue
    public Action<Void> adminPassword(String password) {
        if(!PASSWORD_PREDICATE.test(password)) throw new IllegalArgumentException("Password must be an alphanumeric string of length 0-16");
        return action.new Void(true, "admin.password", password);
    }

    /*
    Broken server-side

    @CheckReturnValue
    public Action<String> adminPassword() {
        return action.new String(true, "admin.password");
    }
     */

    @CheckReturnValue
    public Action<Integer> effectiveMaxPlayers() {
        return action.new Integer(true, "admin.effectiveMaxPlayers");
    }

    @CheckReturnValue
    public Action<Void> say(String message, Subset subset) {
        if (message.length() >= 128) throw new IllegalArgumentException("Message too long");
        return action.new Void(true, "admin.say", message, subset.serialize());
    }

    @CheckReturnValue
    public Action<Void> yell(String message) {
        if (message.length() >= 256) throw new IllegalArgumentException("Message too long");
        return action.new Void(true, "admin.yell", message);
    }

    @CheckReturnValue
    public Action<Void> yell(String message, Duration duration) {
        if (message.length() >= 256) throw new IllegalArgumentException("Message too long");
        return action.new Void(true, "admin.yell", message, duration.toSeconds());
    }

    @CheckReturnValue
    public Action<Void> yell(String message, Subset subset) {
        if (message.length() >= 256) throw new IllegalArgumentException("Message too long");
        return yell(message, subset, Duration.ofSeconds(10));
    }

    @CheckReturnValue
    public Action<Void> yell(String message, Subset subset, Duration duration) {
        if (message.length() >= 256) throw new IllegalArgumentException("Message too long");
        return action.new Void(true, "admin.yell", message, duration.toSeconds(), subset.serialize());
    }

    @CheckReturnValue
    public Action<Void> kickPlayer(String name) {
        return action.new Void(true, "admin.kickPlayer", name);
    }

    @CheckReturnValue
    public Action<Void> kickPlayer(String name, String reason) {
        return action.new Void(true, "admin.kickPlayer", name, reason);
    }

    @CheckReturnValue
    public Action<Void> movePlayer(String name, Team team, Squad squad, boolean forceKill) {
        return action.new Void(true, "admin.movePlayer", name, team.getId(), squad.getId(), forceKill);
    }

    @CheckReturnValue
    public Action<Void> killPlayer(String name) {
        return action.new Void(true, "admin.killPlayer", name);
    }

    @CheckReturnValue
    public Action<Integer> idleDuration(String name) {
        return action.new Integer(true, "player.idleDuration", name);
    }

    @CheckReturnValue
    public Action<Boolean> isAlive(String name) {
        return action.new Boolean(true, "player.isAlive", name);
    }

    @CheckReturnValue
    public Action<Integer> ping(String name) {
        //First parameter according to BF3 R38 spec, second parameter according to reality...
        return action.new Packet(true, "player.ping", name).map(packet -> new WordBuffer(packet.data()).readInt(1));
    }

    @CheckReturnValue
    public Action<List<Squad>> listSquads(Team team) {
        return action.new Packet(true, "squad.listActive", team.getId()).map(packet -> {
            final List<Squad> list = new ArrayList<>();
            final WordBuffer buffer = new WordBuffer(packet.data());

            final int amount = buffer.readInt();
            for (int i = 0; i < amount; i++) list.add(Squad.getById(buffer.readInt()));

            return list;
        });
    }

    @CheckReturnValue
    public Action<List<String>> listPlayers(Team team, Squad squad) {
        return action.new Packet(true, "squad.listPlayers", team.getId(), squad.getId()).map(packet -> {
            final List<String> list = new ArrayList<>();
            final WordBuffer buffer = new WordBuffer(packet.data());

            final int amount = buffer.readInt();
            for (int i = 0; i < amount; i++) list.add(buffer.read());

            return list;
        });
    }

    @CheckReturnValue
    public Action<Boolean> isSquadPrivate(Team team, Squad squad) {
        return action.new Boolean(true, "squad.private", team.getId(), squad.getId());
    }

    @CheckReturnValue
    public Action<Void> setSquadPrivate(Team team, Squad squad, boolean value) {
        return action.new Void(true, "squad.private", team.getId(), squad.getId(), value);
    }

    @CheckReturnValue
    public Action<String> getSquadLeader(Team team, Squad squad) {
        return action.new String(true, "squad.leader", team.getId(), squad.getId());
    }

    @CheckReturnValue
    public Action<Void> setSquadLeader(Team team, Squad squad, String name) {
        return action.new Void(true, "squad.leader", team.getId(), squad.getId(), name);
    }

    @CheckReturnValue
    public Action<Void> activatePunkBuster() {
        return action.new Void(true, "punkBuster.activate");
    }

    @CheckReturnValue
    public Action<Void> issuePunkBusterCommand(String command) {
        // "The entire command is to be sent as a single string. Don’t split it into multiple words." - BF3 R38 spec page 14
        return action.new Void(true, "punkBuster.pb_sv_command", command);
    }

    @CheckReturnValue
    public Action<Void> loadBanList() {
        return action.new Void(true, "banList.load");
    }

    @CheckReturnValue
    public Action<Void> saveBanList() {
        return action.new Void(true, "banList.save");
    }

    @CheckReturnValue
    public Action<Void> addBan(IdType type, String id, Timeout timeout) {
        return action.new Void(true, "banList.add", type.getId(), id, timeout.words());
    }

    @CheckReturnValue
    public Action<Void> addBan(IdType type, String id, Timeout timeout, String reason) {
        if (reason.length() >= 256) throw new IllegalArgumentException("Reason too long");
        return action.new Void(true, "banList.add", type.getId(), id, timeout.words(), reason);
    }

    @CheckReturnValue
    public Action<Void> removeBan(IdType type, String id) {
        return action.new Void(true, "banList.remove", type.getId(), id);
    }

    @CheckReturnValue
    public Action<Void> clearBans() {
        return action.new Void(true, "banList.clear");
    }

    @CheckReturnValue
    public Action<List<BanlistEntry>> listBans(int offset) {
        return action.new Packet(true, "banList.list", offset).map(packet -> {
            final List<BanlistEntry> list = new ArrayList<>();
            final WordBuffer buffer = new WordBuffer(packet.data());

            if (buffer.size() % BanlistEntry.PARAMETER_COUNT != 0)
                throw new AssertionError("Invalid parameter count (Protocol violation)");

            for (int i = 0; i < buffer.size() / BanlistEntry.PARAMETER_COUNT; i++)
                list.add(buffer.readComplex(BanlistEntry::read));

            return list;
        });
    }

    @CheckReturnValue
    public Action<Void> loadGameAdmins() {
        return action.new Void(true, "gameAdmin.load");
    }

    @CheckReturnValue
    public Action<Void> saveGameAdmins() {
        return action.new Void(true, "gameAdmin.save");
    }

    @CheckReturnValue
    public Action<Void> addGameAdmin(String name, int level) {
        if(level < 0 || level > 3) throw new IllegalArgumentException("Level must be between 0 and 3");
        return action.new Void(true, "gameAdmin.add", name, level);
    }

    @CheckReturnValue
    public Action<Void> removeGameAdmin(String name) {
        return action.new Void(true, "gameAdmin.remove", name);
    }

    @CheckReturnValue
    public Action<List<String>> listGameAdmins() {
        return action.new Packet(true, "gameAdmin.list").map(packet -> Arrays.asList(packet.data()));
    }

    @CheckReturnValue
    public Action<Void> loadReservedSlotsList() {
        return action.new Void(true, "reservedSlotList.load");
    }

    @CheckReturnValue
    public Action<Void> saveReservedSlotsList() {
        return action.new Void(true, "reservedSlotList.save");
    }

    @CheckReturnValue
    public Action<Void> addReservedSlot(String name) {
        return action.new Void(true, "reservedSlotList.add", name);
    }

    @CheckReturnValue
    public Action<Void> removeReservedSlot(String name) {
        return action.new Void(true, "reservedSlotList.remove", name);
    }

    @CheckReturnValue
    public Action<Void> clearReservedSlots() {
        return action.new Void(true, "reservedSlotList.clear");
    }

    @CheckReturnValue
    public Action<List<String>> listReservedSlots(int offset) {
        return action.new Packet(true, "reservedSlotList.list", offset).map(packet -> Arrays.asList(packet.data()));
    }

    @CheckReturnValue
    public Action<Void> loadMapList() {
        return action.new Void(true, "mapList.load");
    }

    @CheckReturnValue
    public Action<Void> saveMapList() {
        return action.new Void(true, "mapList.save");
    }

    @CheckReturnValue
    public Action<Void> addMap(GameMap map, GameMode mode, int rounds) {
        return action.new Void(true, "mapList.add", map.getId(), mode.getId(), rounds);
    }

    @CheckReturnValue
    public Action<Void> addMap(GameMap map, GameMode mode, int rounds, int index) {
        return action.new Void(true, "mapList.add", map.getId(), mode.getId(), rounds, index);
    }

    @CheckReturnValue
    public Action<Void> removeMap(int index) {
        return action.new Void(true, "mapList.remove", index);
    }

    @CheckReturnValue
    public Action<Void> clearMaps() {
        return action.new Void(true, "mapList.clear");
    }

    @CheckReturnValue
    public abstract Action<? extends List<? extends MaplistEntry>> listMaps(int offset);

    @CheckReturnValue
    public Action<Void> setNextMapIndex(int index) {
        return action.new Void(true, "mapList.setNextMapIndex", index);
    }

    @CheckReturnValue
    public Action<GameMap.Indices> getMapIndices() {
        return action.new Packet(true, "mapList.getMapIndices").map(packet -> {
            final WordBuffer buffer = new WordBuffer(packet.data());
            return new GameMap.Indices(buffer.readInt(), buffer.readInt());
        });
    }

    @CheckReturnValue
    public Action<GameMap.Rounds> getRounds() {
        return action.new Packet(true, "mapList.getRounds").map(packet -> {
            final WordBuffer buffer = new WordBuffer(packet.data());
            return new GameMap.Rounds(buffer.readInt(), buffer.readInt());
        });
    }

    @CheckReturnValue
    public Action<Void> runNextRound() {
        return action.new Void(true, "mapList.runNextRound");
    }

    @CheckReturnValue
    public Action<Void> restartRound() {
        return action.new Void(true, "mapList.restartRound");
    }

    @CheckReturnValue
    public Action<Void> endRound(Team winner) {
        return action.new Void(true, "mapList.endRound", winner.getId());
    }

    protected <T extends MaplistEntry> Action<List<T>> listMapsTemplate(int offset, Function<WordBuffer, T> function) {
        return action.new Packet(true, "mapList.list", offset).map(packet -> {
            final List<T> list = new ArrayList<>();
            final WordBuffer buffer = new WordBuffer(packet.data());
            final int mapCount = buffer.readInt();
            final int parameterCount = buffer.readInt();
            final int diff = parameterCount - BF3MaplistEntry.PARAMETER_COUNT;

            /*
            The reason for the <number of words per map> specification is future proofing;
            in the future, DICE might add extra words per map after the first three.
            However, the first three words are very likely to remain the same.
             */
            if (diff > 0) {
                getLogger().warn("Received %d extra parameter%s for MapList".formatted(diff, diff == 1 ? "" : "s"));
            }

            for (int i = 0; i < mapCount; i++) {
                list.add(buffer.readComplex(function));
                buffer.jump(diff);
            }

            return list;
        });
    }
}
