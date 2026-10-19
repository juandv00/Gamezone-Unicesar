package com.gamezone.persistence;

import com.gamezone.model.Person;
import java.util.List;

/**
 * Abstraction of the place where the clients and sellers are stored. The services depend on
 * this interface instead of on a concrete file class, so the storage can
 * change without changing the business rules.
 */
public interface PersonStorage {

    /**
     * Saves the given people, replacing the ones previously stored.
     *
     * @param people the clients and sellers to store
     */
    void save(List<Person> people);

    /**
     * Loads the stored people.
     *
     * @return the clients and sellers found, or an empty list if there are none
     */
    List<Person> load();
}
