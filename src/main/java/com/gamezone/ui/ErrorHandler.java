package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;

/**
 * Runs each operation of the submenus and shows a different message for each
 * category of error, so the user never sees a technical message or a stack
 * trace. Every submenu uses this single class, so the error messages are
 * defined in one place.
 */
public class ErrorHandler {

    /**
     * Runs one operation of a submenu and handles its errors. The custom
     * exceptions are caught by their specific type, from the most specific
     * categories to the most general, and each category shows a different
     * message: a resource that was not found, a business rule that was
     * violated, invalid input data, or a problem accessing the data files
     * (whose technical details are never shown). The modules that still use
     * the standard Java exceptions are handled as business rule errors, and
     * any unexpected error shows a general message instead of a stack trace.
     *
     * @param operation the operation to run
     */
    public void executeOperation(Runnable operation) {
        try {
            operation.run();
        } catch (ResourceNotFoundException e) {
            System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
        } catch (BusinessRuleException e) {
            System.out.println("No se puede completar la operación: " + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("No se puede completar la operación: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Ocurrió un error inesperado. Contacte al administrador.");
        }
    }
}
