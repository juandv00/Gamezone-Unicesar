package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Client;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.PersonStorage;
import com.gamezone.validation.PersonValidator;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing people at GameZone Unicesar, such as
 * registering new clients and sellers, and listing the people currently
 * registered in the system. The data and rules are checked by
 * {@link PersonValidator} before each operation, which throws the custom
 * exceptions of the system when something is not valid.
 */
public class PersonService {

    private List<Person> people;
    private final PersonStorage personStorage;

    /**
     * Creates a new PersonService that stores the people in the file of
     * {@link PersonPersistence}. Kept only until the composition root
     * ({@code Main}) injects the storage; use
     * {@link #PersonService(PersonStorage)} instead.
     */
    public PersonService() {
        this(new PersonPersistence());
    }

    /**
     * Creates a new PersonService that uses the given storage, loading the
     * currently stored people from it. The service depends only on the
     * {@link PersonStorage} abstraction, not on a concrete file class.
     *
     * @param personStorage the storage of the clients and sellers
     */
    public PersonService(PersonStorage personStorage) {
        this.personStorage = personStorage;
        this.people = personStorage.load();
    }

    /**
     * Registers a new client and immediately persists the updated list.
     *
     * @param id the client's identification number
     * @param name the client's full name
     * @param phone the client's contact phone number
     * @param email the client's email address
     * @throws InvalidDataException  if a field is empty or the email does not
     * have a valid format
     * @throws BusinessRuleException if another person already has the same id
     */
    public void registerClient(String id, String name, String phone, String email) {
        PersonValidator.validatePersonData(id, name, email == null ? "" : email, phone);
        PersonValidator.validateUniquePerson(people, id);

        Client c = new Client(id, name, phone, email);
        people.add(c);
        personStorage.save(people);
    }

    /**
     * Registers a new seller and immediately persists the updated list.
     *
     * @param id the seller's identification number
     * @param name the seller's full name
     * @param phone the seller's contact phone number
     * @param employeeCode the seller's employee code
     * @param shift the seller's assigned work shift
     * @throws InvalidDataException  if a field is empty
     * @throws BusinessRuleException if another person already has the same id
     */
    public void registerSeller(String id, String name, String phone, String employeeCode, String shift) {
        PersonValidator.validatePersonData(id, name, null, phone);
        PersonValidator.validateSellerData(employeeCode, shift);
        PersonValidator.validateUniquePerson(people, id);

        Seller s = new Seller(id, name, phone, employeeCode, shift);
        people.add(s);
        personStorage.save(people);
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

    /**
     * Finds a registered client by id, failing if no client has that id.
     *
     * @param id the identification number to search for
     * @return the matching client
     * @throws ResourceNotFoundException if no client has the given id
     */
    public Client getClientById(String id) {
        Person person = findById(id);
        PersonValidator.validatePersonExists(person, id);
        if (!(person instanceof Client client)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        return client;
    }

    /**
     * Finds a registered seller by id, failing if no seller has that id.
     *
     * @param id the identification number to search for
     * @return the matching seller
     * @throws ResourceNotFoundException if no seller has the given id
     */
    public Seller getSellerById(String id) {
        Person person = findById(id);
        PersonValidator.validatePersonExists(person, id);
        if (!(person instanceof Seller seller)) {
            throw new ResourceNotFoundException("Vendedor", id);
        }
        return seller;
    }
}
