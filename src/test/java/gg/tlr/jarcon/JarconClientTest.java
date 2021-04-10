package gg.tlr.jarcon;

import gg.tlr.jarcon.bf3.*;
import gg.tlr.jarcon.frostbite.IdType;
import gg.tlr.jarcon.frostbite.Squad;
import gg.tlr.jarcon.frostbite.Subset;
import gg.tlr.jarcon.frostbite.Timeout;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static gg.tlr.jarcon.TestEnv.TEST_PLAYER;
import static gg.tlr.jarcon.TestEnv.newClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JarconClientTest {

    /*
    How to test:
    1. Update constants in TestEnv
    2. Join US Alpha and spawn
    3. Click the button
    4. ???
    5. Profit!
     */

    private static BF3Client client;

    @BeforeAll
    public static void beforeAll() throws Exception {
        client = newClient();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        client.shutdown(false);
    }

    @AfterEach
    public void afterEach() throws Exception {
        sleep(1);
    }

    @Test
    public void send() throws Exception {
        assertEquals("OK", client.getActionFactory().new Packet(false, "version").queue().get().status());
    }

    @Test
    public void serverInfo() throws Exception {
        client.serverInfo().complete();
    }

    @Test
    public void help() throws Exception {
        System.out.println(client.help().queue().get());
    }

    @Test
    public void version() throws Exception {
        client.version().complete();
    }

    @Test
    public void listPlayers() throws Exception {
        client.listPlayers(Subset.all()).complete();
        client.listPlayers(Subset.team(BF3Team.UNASSIGNED)).complete();
        client.listPlayers(Subset.team(BF3Team.US)).complete();
        client.listPlayers(Subset.squad(BF3Team.US, Squad.ALPHA)).complete();
    }

    @Disabled
    @Test
    public void adminPassword() throws Exception {
        client.adminPassword("test").complete();
    }

    @Test
    public void listGameAdmins() throws Exception {
        System.out.println(client.listGameAdmins().queue().get());
    }

    @Test
    public void loadGameAdmins() throws Exception {
        client.loadGameAdmins().complete();
    }

    @Test
    public void saveGameAdmins() throws Exception {
        client.saveGameAdmins().complete();
    }

    @Test
    public void addGameAdmin() throws Exception {
        client.addGameAdmin(TEST_PLAYER, 3).complete();
    }

    @Test
    public void removeGameAdmin() throws Exception {
        client.removeGameAdmin(TEST_PLAYER).complete();
    }

    @Test
    public void effectiveMaxPlayers() throws Exception {
        client.effectiveMaxPlayers().complete();
    }

    @Test
    public void say() throws Exception {
        client.say("Test", Subset.all()).complete();
    }

    @Test
    public void sayTeam() throws Exception {
        client.say("Test", Subset.team(BF3Team.US)).complete();
    }

    @Test
    public void yell() throws Exception {
        client.yell("Test").complete();
    }

    @Test
    public void testYellTeam() throws Exception {
        client.yell("Test", Subset.team(BF3Team.US)).complete();
    }

    @Test
    public void testYellTimed() throws Exception {
        client.yell("Test", Duration.ofSeconds(5)).complete();
    }

    @Test
    public void testYellTeamTimed() throws Exception {
        client.yell("Test", Subset.team(BF3Team.RU), Duration.ofSeconds(5)).complete();
    }

    @Disabled
    @Order(100)
    @Test
    public void kickPlayer() throws Exception {
        client.kickPlayer(TEST_PLAYER, "Test").complete();
    }

    @Order(10)
    @Test
    public void movePlayer() throws Exception {
        client.movePlayer(TEST_PLAYER, BF3Team.RU, Squad.NONE, true).complete();
        Thread.sleep(1000);
        client.movePlayer(TEST_PLAYER, BF3Team.US, Squad.ALPHA, true).complete();
    }

    @Order(1)
    @Test
    public void killPlayer() throws Exception {
        client.killPlayer(TEST_PLAYER).complete();
    }

    @Order(1)
    @Test
    public void idleDuration() throws Exception {
        client.idleDuration(TEST_PLAYER).complete();
    }

    @Order(1)
    @Test
    public void isAlive() throws Exception {
        client.isAlive(TEST_PLAYER).complete();
    }

    @Order(1)
    @Test
    public void ping() throws Exception {
        client.ping(TEST_PLAYER).complete();
    }

    @Test
    public void listSquads() throws Exception {
        client.listSquads(BF3Team.US).complete();
    }

    @Order(1)
    @Test
    public void isSquadPrivate() throws Exception {
        client.isSquadPrivate(BF3Team.US, Squad.ALPHA).complete();
    }

    @Order(1)
    @Test
    public void setSquadPrivate() throws Exception {
        client.setSquadPrivate(BF3Team.US, Squad.ALPHA, true).complete();
    }

    @Order(1)
    @Test
    public void getSquadLeader() throws Exception {
        client.getSquadLeader(BF3Team.US, Squad.ALPHA).complete();
    }

    @Order(1)
    @Test
    public void setSquadLeader() throws Exception {
        client.setSquadLeader(BF3Team.US, Squad.ALPHA, TEST_PLAYER).complete();
    }

    @Disabled
    @Test
    public void activatePunkBuster() throws Exception {
        client.activatePunkBuster().complete();
    }

    @Test
    public void issuePunkBusterCommand() throws Exception {
        client.issuePunkBusterCommand("pb_sv_plist").complete();
    }

    @Test
    public void loadBanList() throws Exception {
        client.loadBanList().complete();
    }

    @Test
    public void saveBanList() throws Exception {
        client.saveBanList().complete();
    }

    @Disabled
    @Order(1000)
    @Test
    public void addBan() throws Exception {
        client.addBan(IdType.NAME, TEST_PLAYER, Timeout.duration(Duration.ofMinutes(1))).complete();
    }

    @Disabled
    @Order(1001)
    @Test
    public void testAddBan() throws Exception {
        client.addBan(IdType.NAME, TEST_PLAYER, Timeout.duration(Duration.ofMinutes(1)), "Test").complete();
    }

    @Disabled
    @Order(1002)
    @Test
    public void removeBan() throws Exception {
        client.removeBan(IdType.NAME, TEST_PLAYER).complete();
    }

    @Disabled
    @Test
    public void clearBans() throws Exception {
        client.clearBans().complete();
    }

    @Test
    public void listBans() throws Exception {
        client.listBans(0).complete();
    }

    @Test
    public void loadMapList() throws Exception {
        client.loadMapList().complete();
    }

    @Test
    public void saveMapList() throws Exception {
        client.saveMapList().complete();
    }

    @Disabled
    @Order(2)
    @Test
    public void addMap() throws Exception {
        client.addMap(BF3Map.OPERATION_FIRESTORM, BF3Mode.CONQUEST_LARGE, 1).complete();
    }

    @Disabled
    @Test
    public void testAddMap() throws Exception {
        client.addMap(BF3Map.OPERATION_METRO, BF3Mode.CONQUEST_LARGE, 1, 0).complete();
    }

    @Disabled
    @Order(3)
    @Test
    public void removeMap() throws Exception {
        client.removeMap(0).complete();
    }

    @Disabled
    @Test
    public void clearMaps() throws Exception {
        client.clearMaps().complete();
    }

    @Test
    public void listMaps() throws Exception {
        client.listMaps(0).complete();
    }

    @Disabled
    @Test
    public void setNextMapIndex() throws Exception {
        client.setNextMapIndex(0).complete();
    }

    @Test
    public void getMapIndices() throws Exception {
        client.getMapIndices().complete();
    }

    @Test
    public void getRounds() throws Exception {
        client.getRounds().complete();
    }

    @Disabled
    @Order(50)
    @Test
    public void runNextRound() throws Exception {
        sleep(5);
        client.runNextRound().complete();
        sleep(15);
    }

    @Disabled
    @Order(51)
    @Test
    public void restartRound() throws Exception {
        int duration = 5;
        getSleep(TimeUnit.SECONDS.toMillis(duration));
        client.restartRound().complete();
        sleep(15);
    }

    private void getSleep(long l) throws InterruptedException {
        Thread.sleep(l);
    }

    @Disabled
    @Order(49)
    @Test
    public void endRound() throws Exception {
        sleep(5);
        client.endRound(BF3Team.US).complete();
        sleep(5);
    }

    @Test
    public void getVar() throws Exception {
        client.getVar(BF3Vars.FRIENDLY_FIRE).complete();
    }

    @Test
    public void setVar() throws Exception {
        final Integer timeout = client.getVar(BF3Vars.IDLE_BAN_ROUNDS).complete();
        client.setVar(BF3Vars.IDLE_BAN_ROUNDS, timeout).complete();
        client.setVar(BF3Vars.IDLE_BAN_ROUNDS, 1).complete();
        assertThrows(IllegalArgumentException.class, () -> client.setVar(BF3Vars.GAME_PASSWORD, "Test").complete());
        assertThrows(IllegalArgumentException.class, () -> client.setVarClamped(BF3Vars.IDLE_BAN_ROUNDS, 1).complete());
    }
    
    private static void sleep(int seconds) throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
    }

    @Disabled
    @Test
    void logout() {
    }

    @Disabled
    @Test
    void quit() {
    }

    @Test
    void eventsEnabled() throws Exception {
        client.eventsEnabled(true).complete();

        final CompletableFuture<Void> f = new CompletableFuture<>();
        client.getEventHandler().registerListener(new BF3EventListener() {
            @Override
            public void onChat(String source, String message, Subset subset) {
                f.complete(null);
            }
        });
        System.out.println("Waiting for chat event...");
        client.say("Trigger chat event", Subset.all()).queue();
        f.orTimeout(30, TimeUnit.SECONDS).get();

        client.eventsEnabled(false).complete();
    }
}