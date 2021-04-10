package gg.tlr.jarcon.core;

import gg.tlr.jarcon.frostbite.FrostbiteError;

public class ErrorResponseException extends RuntimeException {
    private final String      id;
    private final RemoteError error;

    private ErrorResponseException(String error) {
        super("Unknown error: %s".formatted(error));
        this.id = error;
        this.error = FrostbiteError.UNKNOWN;
    }

    private ErrorResponseException(RemoteError error) {
        super(createExceptionMessage(error));
        if (error == FrostbiteError.UNKNOWN) throw new IllegalArgumentException("Must specify error message");
        this.id = error.getId();
        this.error = error;
    }

    public String getErrorId() {
        return id;
    }

    public RemoteError getError() {
        return error;
    }

    public FrostbiteError getFrostbiteError() {
        return error instanceof FrostbiteError fe ? fe : FrostbiteError.UNKNOWN;
    }

    public static ErrorResponseException create(String error) {
        return FrostbiteError.getById(error)
                .map(ErrorResponseException::new)
                .orElseGet(() -> new ErrorResponseException(error));
    }

    private static String createExceptionMessage(RemoteError error) {
        if(error instanceof FrostbiteError fe) return fe.name();
        else return "%s error: %s".formatted(error.getProviderName(), error.getId());
    }
}
