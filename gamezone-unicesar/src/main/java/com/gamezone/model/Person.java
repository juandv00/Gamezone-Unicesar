package com.gamezone.model;

/**
 * Represents a generic person who interacts with GameZone Unicesar. This class
 * defines the attributes common to all people in the system and must be
 * extended by specific roles (e.g. Client, Seller).
 */
public abstract class Person {

    private String id;
    private String name;
    private String phone;

    /**
     * Creates a new Person with the given common attributes.
     *
     * @param id unique identification number of the person
     * @param name full name of the person
     * @param phone contact phone number of the person
     */
    public Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Person{" + "id=" + id + ", name=" + name + ", phone=" + phone + '}';
    }
}
