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
    private PersonPersistence personpersistence;

    /**
     * Creates a new PersonService, loading the currently stored people from the
     * repository.
     */
    public PersonService() {
        this.personpersistence = new PersonPersistence();
        this.people = personpersistence.load();
    }

    /**
     * Registers a new client and immediately persists the updated list.
     *
     * @param id the client's identification number
     * @param name the client's full name
     * @param phone the client's contact phone number
     * @param email the client's email address
     */
    public void RegisterClient(String id, String name, String phone, String email) {
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
        personpersistence.save(people);
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
    public void regissterseller(String id, String name, String phone, String employeeCode, String shift) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Client id cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Client phone cannot be empty.");
        }
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        if (shift == null || shift.trim().isEmpty()) {
            throw new IllegalArgumentException("Client phone cannot be empty.");
        }

        Seller s = new Seller(id, name, phone, employeeCode, shift);
        people.add(s);
        personpersistence.save(people);
    }

    /**
     * Returns the list of all clients currently registered.
     *
     * @return the list of registered clients
     */
    public List<Client> listClient() {
        List<Client> Customers = new ArrayList<>();
        for (Client c : Customers) {
            if (c instanceof Client) {
                Customers.add((Client) c);
            }
        }
        return Customers;
    }

    /**
     * Returns the list of all sellers currently registered.
     *
     * @return the list of registered sellers
     */
    public List<Seller> listSeller() {
        List<Seller> traders = new ArrayList<>();
        for (Seller s : traders) {
            if (s instanceof Seller) {
                traders.add((Seller) s);
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
