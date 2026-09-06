package com.gamezone.model;

public class Client extends Person {

    private String email;

    public Client() {
    }

    public Client(String id, String name, int phone, String email) {
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
