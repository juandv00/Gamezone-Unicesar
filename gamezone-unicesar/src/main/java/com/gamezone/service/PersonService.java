package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonPersistence;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing people at GameZone Unicesar, such as
 * registering new clients and sellers, and listing the people currently
 * registered in the system.
 */
public class PersonService {

    private List<Person> people;
    private PersonPersistence personPersistence;

    /**
     * Creates a new PersonService, loading the currently stored people from the
     * repository.
     */
    public PersonService() {
        this.personPersistence = new PersonPersistence();
        this.people = personPersistence.load();
    }

    /**
     * Registers a new client and immediately persists the updated list.
     *
     * @param id the client's identification number
     * @param name the client's full name
     * @param phone the client's contact phone number
     * @param email the client's email address
     */
    public void registerClient(String id, String name, String phone, String email) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Client id cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Client phone cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Client email cannot be empty.");
        }

        Client c = new Client(id, name, phone, email);
        people.add(c);
        personPersistence.save(people);
    }

    /**
     * Registers a new seller and immediately persists the updated list.
     *
     * @param id the seller's identification number
     * @param name the seller's full name
     * @param phone the seller's contact phone number
     * @param employeeCode the seller's employee code
     * @param shift the seller's assigned work shift
     */
    public void registerSeller(String id, String name, String phone, String employeeCode, String shift) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Seller id cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Seller name cannot be empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Seller phone cannot be empty.");
        }
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be empty.");
        }
        if (shift == null || shift.trim().isEmpty()) {
            throw new IllegalArgumentException("Shift cannot be empty.");
        }

        Seller s = new Seller(id, name, phone, employeeCode, shift);
        people.add(s);
        personPersistence.save(people);
    }

    /**
     * Returns the list of all clients currently registered.
     *
     * @return the list of registered clients
     */
    public List<Client> listClient() {
        List<Client> customers = new ArrayList<>();
        for (Person person : people) {
            if (person instanceof Client c) {
                customers.add(c);
            }
        }
        return customers;
    }

    /**
     * Returns the list of all sellers currently registered.
     *
     * @return the list of registered sellers
     */
    public List<Seller> listSeller() {
        List<Seller> traders = new ArrayList<>();
        for (Person person : people) {
            if (person instanceof Seller s) {
                traders.add(s);
            }
        }
        return traders;
    }

    /**
     * Finds a person by their unique identification number.
     *
     * @param id the identification number to search for
     * @return the matching person, or null if not found
     */
    public Person findById(String id) {
        for (Person person : people) {
            if (person.getId().equals(id)) {
                return person;
            }
        }
        return null;
    }
}
