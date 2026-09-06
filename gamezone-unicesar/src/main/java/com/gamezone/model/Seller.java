package com.gamezone.model;

/**
 * Represents a seller employed at GameZone Unicesar. A seller is a specific
 * type of Person who attends clients and registers sales, identified
 * additionally by an employee code and shift.
 */
public class Seller extends Person {

    private String employeeCode;
    private String shift;

    /**
     * Creates a new Seller with the given common and particular attributes.
     *
     * @param id unique identification number of the seller
     * @param name full name of the seller
     * @param phone contact phone number of the seller
     * @param employeeCode unique employee code assigned to the seller
     * @param shift work shift assigned to the seller (e.g. "Morning")
     */
    public Seller(String employeeCode, String shift, String id, String name, String phone) {
        super(id, name, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    @Override
    public String toString() {
        return super.toString() + "Seller{" + "employeeCode=" + employeeCode + ", shift=" + shift + '}';
    }
}
