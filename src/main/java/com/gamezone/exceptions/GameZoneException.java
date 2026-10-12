package com.gamezone.exceptions;

/**
 * Root of the custom exception hierarchy of GameZone Unicesar. Every error
 * that the system reports on purpose extends this class, so the user
 * interface can catch each category separately (resource not found,
 * business rule, invalid data, or persistence) or all of them together.
 * It is unchecked (it extends RuntimeException), so the methods that throw
 * it do not need to declare it in their signatures.
 */
public abstract class GameZoneException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    /**
     * Creates a new exception with the given message and no error code.
     *
     * @param message the description of the error, shown to the user
     */
    protected GameZoneException(String message) {
        this(message, null);
    }

    /**
     * Creates a new exception with the given message and error code.
     *
     * @param message   the description of the error, shown to the user
     * @param errorCode a short code that identifies the category of the error
     */
    protected GameZoneException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the code that identifies the category of this error.
     *
     * @return the error code, or null if none was given
     */
    public String getErrorCode() {
        return errorCode;
    }
}
