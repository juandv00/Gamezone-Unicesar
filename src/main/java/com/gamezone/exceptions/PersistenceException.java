package com.gamezone.exceptions;

/**
 * Thrown when a data file cannot be read or written: a corrupted file, a
 * denied permission, or a full disk, among others. It keeps the original
 * exception as its cause, so the technical details are not lost even though
 * the user only sees a general message.
 */
public class PersistenceException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "PERSISTENCE";

    /**
     * Creates a new exception with a message that describes the failed
     * operation and the original cause.
     *
     * @param message the description of the failed operation, including the
     *                file and whether it was a read or a write
     * @param cause   the original exception (usually an IOException)
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, ERROR_CODE);
        initCause(cause);
    }
}
