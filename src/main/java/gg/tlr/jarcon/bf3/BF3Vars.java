package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.Util;
import gg.tlr.jarcon.core.Var;
import gg.tlr.jarcon.frostbite.UnlockMode;

import java.util.List;

import static gg.tlr.jarcon.core.Var.Flag.*;

@SuppressWarnings("unused")
public class BF3Vars {

    //TODO Flags
    public static final Var<Boolean>    RANKED                              = new Var.Boolean("ranked", READ_ONLY);
    public static final Var<String>     SERVER_NAME                         = new Var.String("serverName");
    public static final Var<String>     GAME_PASSWORD                       = new Var.String("gamePassword", READ_ONLY);
    public static final Var<Boolean>    AUTO_BALANCE                        = new Var.Boolean("autoBalance");
    public static final Var<Boolean>    FRIENDLY_FIRE                       = new Var.Boolean("friendlyFire");
    public static final Var<Integer>    MAX_PLAYERS                         = new Var.Integer("maxPlayers") {
        @Override
        protected void validate(java.lang.Integer value) {
            if (value < 8 || value > 64) iae("Value out of bounds");
        }
    };
    public static final Var<String>     SERVER_DESCRIPTION                  = new Var.String("serverDescription") {
        @Override
        protected void validate(java.lang.String value) {
            if (value.length() >= 256) iae("Value too long");
        }
    };
    public static final Var<String>     SERVER_MESSAGE                      = new Var.String("serverMessage") {
        @Override
        protected void validate(java.lang.String value) {
            if (value.length() >= 256) iae("Value too long");
        }
    };
    public static final Var<Boolean>    KILL_CAM                            = new Var.Boolean("killCam");
    public static final Var<Boolean>    KILL_ROTATION                       = new Var.Boolean("killRotation", READ_ONLY);
    public static final Var<Boolean>    MINIMAP                             = new Var.Boolean("miniMap");
    public static final Var<Boolean>    HUD                                 = new Var.Boolean("hud");
    //Broken server-side
    //public static final Var<Boolean>    CROSSHAIR                           = new Var.Boolean("crossHair");
    public static final Var<Boolean>    SPOTTING_3D                         = new Var.Boolean("3dSpotting");
    public static final Var<Boolean>    SPOTTING_MINIMAP                    = new Var.Boolean("miniMapSpotting");
    public static final Var<Boolean>    NAMETAGS                            = new Var.Boolean("nameTag");
    public static final Var<Boolean>    THIRD_PERSON_CAM                    = new Var.Boolean("3pCam");
    public static final Var<Boolean>    REGENERATE_HEALTH                   = new Var.Boolean("regenerateHealth");
    public static final Var<Integer>    TEAM_KILL_COUNT_KICK                = new Var.Integer("teamKillCountForKick");
    public static final Var<Integer>    TEAM_KILL_VALUE_KICK                = new Var.Integer("teamKillValueForKick");
    public static final Var<Integer>    TEAM_KILL_VALUE_INCREASE            = new Var.Integer("teamKillValueIncrease");
    public static final Var<Integer>    TEAM_KILL_VALUE_DECREASE_PER_SECOND = new Var.Integer("teamKillValueDecreasePerSecond");
    public static final Var<Integer>    TEAM_KILL_KICK_FOR_BAN              = new Var.Integer("teamKillKickForBan");
    public static final Var<Integer>    IDLE_TIMEOUT                        = new Var.Integer("idleTimeout");
    public static final Var<Integer>    IDLE_BAN_ROUNDS                     = new Var.Integer("idleBanRounds");
    public static final Var<Integer>    ROUND_START_PLAYER_COUNT            = new Var.Integer("roundStartPlayerCount", CLAMPED);
    public static final Var<Integer>    ROUND_RESTART_PLAYER_COUNT          = new Var.Integer("roundRestartPlayerCount", CLAMPED);
    public static final Var<Integer>    ROUND_LOCKDOWN_COUNTDOWN            = new Var.Integer("roundLockdownCountdown", CLAMPED) {
        @Override
        protected void validate(java.lang.Integer value) {
            if (value < 15 || value > 900) iae("Value out of bounds");
        }
    };
    public static final Var<Boolean>    VEHICLE_SPAWN_ALLOWED               = new Var.Boolean("vehicleSpawnAllowed", CLAMPED);
    public static final Var<Integer>    VEHICLE_SPAWN_DELAY                 = new Var.Integer("vehicleSpawnDelay");
    public static final Var<Integer>    SOLDIER_HEALTH                      = new Var.Integer("soldierHealth");
    public static final Var<Integer>    PLAYER_RESPAWN_TIME                 = new Var.Integer("playerRespawnTime");
    public static final Var<Integer>    PLAYER_MAN_DOWN_TIME                = new Var.Integer("playerManDownTime");
    public static final Var<Integer>    BULLET_DAMAGE                       = new Var.Integer("bulletDamage");
    public static final Var<Integer>    GAMEMODE_COUNTER                    = new Var.Integer("gameModeCounter");
    public static final Var<Boolean>    ONLY_SQUAD_LEADER_SPAWN             = new Var.Boolean("onlySquadLeaderSpawn");
    public static final Var<UnlockMode> UNLOCK_MODE                         = new Var.Enum<>("unlockMode", UnlockMode.class);
    public static final Var<Boolean>    PREMIUM_STATUS                      = new Var.Boolean("premiumStatus");
    public static final Var<Integer>    GUNMASTER_WEAPONS_PRESET            = new Var.Integer("gunMasterWeaponsPreset");
    public static final Var<Boolean>    AGGRESSIVE_JOIN                     = new Var.Boolean("reservedSlotsList.aggressiveJoin", NON_VAR);
    public static final Var<Boolean>    PUNKBUSTER_ACTIVE                   = new Var.Boolean("punkBuster.isActive", READ_ONLY, NON_VAR);

    private static final List<Var<?>> VARS = Util.listVars(BF3Vars.class);

    public static List<Var<?>> vars() {
        return VARS;
    }
}
