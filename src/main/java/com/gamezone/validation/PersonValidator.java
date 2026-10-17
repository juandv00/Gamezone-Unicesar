package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Person;
import java.util.List;

/**
 * Groups the validations of the person module (clients and sellers). Its
 * methods are static and are called by the services before executing a
 * business operation; each one throws the corresponding custom exception
 * when a rule is not met. Validators do not access services or repositories.
 */
public final class PersonValidator {

    private PersonValidator() {
        // Utility class: it is not meant to be instantiated.
    }

    /**
     * Validates the common data of a person: the id, the name, and the phone
     * cannot be null or empty. The email is validated only when it is given
     * (clients have an email, sellers do not): it cannot be empty and it must
     * have a reasonable format (an at sign followed by a dot).
     *
     * @param id    the identification number
     * @param name  the full name
     * @param email the email address, or null for people without email
     * @param phone the contact phone number
     * @throws InvalidDataException if any value is not valid
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        requireText("identificación", id);
        requireText("nombre", name);
        requireText("teléfono", phone);
        if (email != null) {
            requireText("correo electrónico", email);
            int at = email.indexOf('@');
            if (at <= 0 || email.indexOf('.', at) < at + 2 || email.endsWith(".")) {
                throw new InvalidDataException("correo electrónico",
                        "debe tener un formato válido, por ejemplo nombre@correo.com.");
            }
        }
    }

    /**
     * Validates the data that only sellers have: the employee code and the
     * shift cannot be null or empty.
     *
     * @param employeeCode the seller's employee code
     * @param shift        the seller's work shift
     * @throws InvalidDataException if any value is empty
     */
    public static void validateSellerData(String employeeCode, String shift) {
        requireText("código de empleado", employeeCode);
        requireText("turno", shift);
    }

    /**
     * Validates that a person was found.
     *
     * @param person   the person returned by the search, or null
     * @param personId the id that was searched
     * @throws ResourceNotFoundException if the person is null
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("Persona", personId);
        }
    }

    /**
     * Validates that no registered person already uses the given id.
     *
     * @param existing the people already registered
     * @param newId    the id of the person to register
     * @throws BusinessRuleException if another person has the same id
     */
    public static void validateUniquePerson(List<Person> existing, String newId) {
        for (Person person : existing) {
            if (person.getId().equals(newId)) {
                throw new BusinessRuleException("Ya existe una persona registrada con la identificación "
                        + newId + ".");
            }
        }
    }

    private static void requireText(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(fieldName, "no puede estar vacío.");
        }
    }
}
