package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles saving and loading Person data (clients and sellers) to and from a
 * file, so that this information persists between application runs. Any
 * error while reading or writing the file is reported as a
 * {@link PersistenceException} that names the file and the operation.
 * <p>
 * The format of each person type is registered once in the constructor: a
 * writer for its class and a reader for its type code. Saving and loading
 * look up the registered format instead of checking the type with a chain
 * of conditionals, so a new person role only needs a new registration.
 */
public class PersonPersistence {

    private static final String FILE_PATH = "data/persons.txt";
    private static final String DELIMITER = "\t";

    private final Map<Class<? extends Person>, Function<Person, String>> writers = new HashMap<>();
    private final Map<String, PersonReader> readers = new HashMap<>();

    /**
     * Rebuilds a person of one type from the fields of a stored line.
     */
    @FunctionalInterface
    private interface PersonReader {

        /**
         * Creates the person from its common fields and the stored line.
         *
         * @param id    the identification of the person
         * @param name  the name of the person
         * @param phone the phone number of the person
         * @param parts every field of the stored line
         * @return the reconstructed person
         */
        Person read(String id, String name, String phone, String[] parts);
    }

    /**
     * Creates the persistence handler and registers the file format of each
     * person type (clients and sellers).
     */
    public PersonPersistence() {
        registerFormat(Client.class, "CLIENT",
                Client::getEmail,
                (id, name, phone, parts) -> new Client(id, name, phone, parts[4]));
        registerFormat(Seller.class, "SELLER",
                seller -> seller.getEmployeeCode() + DELIMITER + seller.getShift(),
                (id, name, phone, parts) -> new Seller(id, name, phone, parts[4], parts[5]));
    }

    /**
     * Registers how a person type is written to and read from the file.
     * Every line starts with the type code followed by the common fields
     * (id, name, phone) and then the fields of the type.
     *
     * @param type           the class of the person type
     * @param code           the type code stored at the start of the line
     * @param specificFields builds the fields of the type, already delimited
     * @param reader         rebuilds a person of the type from a line
     * @param <T>            the person type
     */
    private <T extends Person> void registerFormat(Class<T> type, String code,
                                                   Function<T, String> specificFields,
                                                   PersonReader reader) {
        writers.put(type, person -> code + DELIMITER + person.getId() + DELIMITER
                + person.getName() + DELIMITER + person.getPhone() + DELIMITER
                + specificFields.apply(type.cast(person)));
        readers.put(code, reader);
    }

    /**
     * Saves the given list of people to the data file, overwriting any
     * previously stored content.
     *
     * @param people the list of people to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void save(List<Person> people) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Person p : people) {
                bw.write(toLine(p));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de escritura en el archivo " + FILE_PATH
                    + " al guardar las personas.", e);
        }
    }

    /**
     * Loads the list of people stored in the data file.
     *
     * @return the list of people found, or an empty list if the file does not
     * exist yet
     * @throws PersistenceException if the file cannot be read or contains a
     * line with an invalid format
     */
    public List<Person> load() {
        List<Person> people = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return people;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Person person = parseLine(line);
                if (person != null) {
                    people.add(person);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de lectura en el archivo " + FILE_PATH
                    + " al cargar las personas.", e);
        }
        return people;
    }

    /**
     * Converts a Person into a single delimited line of text for storage.
     *
     * @param person the person to convert
     * @return the text line representing the person
     */
    private String toLine(Person person) {
        Function<Person, String> writer = writers.get(person.getClass());
        if (writer == null) {
            return "";
        }
        return writer.apply(person);
    }

    /**
     * Parses a stored line, reporting a line with an invalid value as a
     * PersistenceException (the file is corrupted).
     *
     * @param line the text line to parse
     * @return the reconstructed Person, or null if the line is incomplete
     * @throws PersistenceException if the line has an invalid value
     */
    private Person parseLine(String line) {
        try {
            return fromLine(line);
        } catch (RuntimeException e) {
            throw new PersistenceException("Error de lectura en el archivo " + FILE_PATH
                    + ": la línea \"" + line + "\" tiene un formato inválido.", e);
        }
    }

    /**
     * Parses a single delimited line of text back into a Person instance.
     *
     * @param line the text line to parse
     * @return the reconstructed Person, or null if the line is invalid
     */
    private Person fromLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 5) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        String phone = parts[3];

        PersonReader reader = readers.get(type);
        if (reader == null) {
            return null;
        }
        return reader.read(id, name, phone, parts);
    }
}
