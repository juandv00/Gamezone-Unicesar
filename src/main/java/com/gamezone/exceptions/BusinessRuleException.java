package com.gamezone.exceptions;

/**
 * Thrown when an operation violates a business rule of GameZone Unicesar,
 * such as insufficient stock, a duplicated client, or a sale without items.
 */
public class BusinessRuleException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "BUSINESS_RULE";

    /**
     * Creates a new exception describing the business rule that was violated.
     *
     * @param message the description of the violated rule, in Spanish
     */
    public BusinessRuleException(String message) {
        super(message, ERROR_CODE);
    }
}
