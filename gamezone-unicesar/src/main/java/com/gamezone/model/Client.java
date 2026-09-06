
package com.gamezone.model;


public class Client extends Person {
    private String email;
    private String pruchase_history;

    public Client() {
    }

    public Client( String id, String name, int phone, String email, String pruchase_history) {
        super(id, name, phone);
        this.email = email;
        this.pruchase_history = pruchase_history;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPruchase_history() {
        return pruchase_history;
    }

    public void setPruchase_history(String pruchase_history) {
        this.pruchase_history = pruchase_history;
    }

    @Override
    public String toString() {
        return "Client{" + "email=" + email + ", pruchase_history=" + pruchase_history + '}';
    }
    
    

   
    

   
    
    
}
