package com.gamezone.exceptions;

/**
 * Thrown when an input value does not meet the expected format, range, or
 * type, such as a negative price, a negative quantity, or an empty text
 * where content is required.
 */
public class InvalidDataException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "INVALID_DATA";

    private final String fieldName;

    /**
     * Creates a new exception for the given field, building a descriptive
     * message in Spanish, for example
     * "El campo 'precio' no es válido: debe ser mayor que cero."
     *
     * @param fieldName the name of the invalid field, in Spanish
     * @param problem   the description of the problem, in Spanish
     */
    public InvalidDataException(String fieldName, String problem) {
        super("El campo '" + fieldName + "' no es válido: " + problem, ERROR_CODE);
        this.fieldName = fieldName;
    }

    /**
     * Returns the name of the invalid field.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }
}
