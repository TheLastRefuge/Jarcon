package gg.tlr.jarcon.frostbite;

import gg.tlr.jarcon.core.RemoteError;

import java.util.Optional;

public enum FrostbiteError implements RemoteError {

    UNKNOWN(null),
    LOGIN_REQUIRED("LogInRequired"),
    UNKNOWN_COMMAND("UnknownCommand"),
    INVALID_ARGUMENTS("InvalidArguments"),
    INVALID_PASSWORD("InvalidPassword"),
    PASSWORD_NOT_SET("PasswordNotSet"),
    INVALID_PASSWORD_HASH("InvalidPasswordHash"),
    INVALID_PLAYER_NAME("InvalidPlayerName"),
    INVALID_TEAM("InvalidTeam"),
    INVALID_SQUAD("InvalidSquad"),
    EMPTY_SQUAD("EmptySquad"),
    INVALID_PB_SERVER_COMMAND("InvalidPbServerCommand"),
    PLAYER_NOT_FOUND("PlayerNotFound"),
    TOO_LONG_MESSAGE("TooLongMessage"),
    MESSAGE_IS_TOO_LONG("MessageIsTooLong"),
    INVALID_TEAM_ID("InvalidTeamId"),
    INVALID_SQUAD_ID("InvalidSquadId"),
    INVALID_FORCE_KILL("InvalidForceKill"),
    PLAYER_NOT_DEAD("PlayerNotDead"),
    SET_TEAM_FAILED("SetTeamFailed"),
    SET_SQUAD_FAILED("SetSquadFailed"),
    SOLDIER_NOT_ALIVE("SoldierNotAlive"),
    PLAYER_ALREADY_IN_LIST("PlayerAlreadyInList"),
    FULL("Full"),
    INVALID_NAME("InvalidName"),
    ACCESS_ERROR("AccessError"),
    INCOMPLETE_READ("IncompleteRead"),
    PLAYER_NOT_IN_LIST("PlayerNotInList"),
    INVALID_ID_TYPE("InvalidIdType"),
    INVALID_BAN_TYPE("InvalidBanType"),
    INVALID_TIME_STAMP("InvalidTimeStamp"),
    INCOMPLETE_BAN("IncompleteBan"),
    BAN_LIST_FULL("BanListFull"),
    NOT_FOUND("NotFound"),
    INVALID_MAP("InvalidMap"),
    INVALID_GAME_MODE_ON_MAP("InvalidGameModeOnMap"),
    INVALID_ROUNDS_PER_MAP("InvalidRoundsPerMap"),
    INVALID_MAP_INDEX("InvalidMapIndex"),
    TOO_LONG_NAME("TooLongName"),
    INVALID_CONFIG("InvalidConfig"),
    LEVEL_NOT_LOADED("LevelNotLoaded"),
    INVALID_NR_OF_PLAYERS("InvalidNrOfPlayers"),
    INVALID_RESTRICTION_LEVEL("InvalidRestrictionLevel");

    private final String id;

    FrostbiteError(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getProviderName() {
        return "Frostbite";
    }

    public static Optional<FrostbiteError> getById(String response) {
        for (FrostbiteError value : values()) if (response.equals(value.getId())) return Optional.of(value);

        return Optional.empty();
    }
}
