
package com.gamezone.model;


public class Seller extends Person {
    private int employee_code;
    private int work_shift;

    public Seller() {
    }

    public Seller(int employee_code, int work_shift, String id, String name, int phone) {
        super(id, name, phone);
        this.employee_code = employee_code;
        this.work_shift = work_shift;
    }

    public int getEmployee_code() {
        return employee_code;
    }

    public void setEmployee_code(int employee_code) {
        this.employee_code = employee_code;
    }

    public int getWork_shift() {
        return work_shift;
    }

    public void setWork_shift(int work_shift) {
        this.work_shift = work_shift;
    }

    @Override
    public String toString() {
        return "Seller{" + "employee_code=" + employee_code + ", work_shift=" + work_shift + '}';
    }
    
    
}
