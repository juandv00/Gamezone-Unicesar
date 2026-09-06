package com.gamezone.model;

public class Seller extends Person {

    private int employeeCode;
    private int shift;

    public Seller() {
    }

    public Seller(String id, String name, int phone, int employeeCode, int shift) {
        super(id, name, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    public int getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(int employeeCode) {
        this.employeeCode = employeeCode;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    @Override
    public String toString() {
        return "Seller{" + "employeeCode=" + employeeCode + ", shift=" + shift + '}';
    }

}
