package com.gamezone.model;

/**
 * Represents a client of GameZone Unicesar. A client is a specific type of
 * Person who purchases products, identified additionally by an email address.
 */
public class Client extends Person {

    private String email;

    /**
     * Creates a new Client with the given common and particular attributes.
     *
     * @param id unique identification number of the client
     * @param name full name of the client
     * @param phone contact phone number of the client
     * @param email email address of the client
     */
    public Client(String id, String name, String phone,String email) {
        super(id, name, phone);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return super.toString() + "Client{" + "email=" + email + '}';
    }
}
