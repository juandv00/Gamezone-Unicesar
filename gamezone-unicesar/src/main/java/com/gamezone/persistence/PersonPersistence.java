package com.gamezone.persistence;

import com.gamezone.model.Client;
import java.io.*;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import java.util.List;
import java.util.ArrayList;

public class PersonPersistence {

    private static final String FILE_PATH = "data/Person.txt";

    public void save(List<Person> people) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Person p : people) {

                if (p != null && p.getClass() == Client.class) {
                    Client c = (Client) p;
                    bw.write("CLIENT:" + "\t" + c.getId() + "\t" + c.getName() + "\t" + c.getPhone() + c.getEmail());
                    bw.newLine();
                } else if (p != null && p.getClass() == Seller.class) {
                    Seller s = (Seller) p;
                    bw.write("SELLER:" + "\t" + s.getId() + "\t" + s.getName() + "\t" + s.getPhone()
                            + s.getEmployeeCode() + "\t" + s.getShift());
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("Error saving Person" + e.getMessage());
        }

    }

    public List<Person> load() {
        List<Person> people = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return people;
        }

        try (BufferedReader rd = new BufferedReader(new FileReader(file))) {

        } catch (IOException b) {
        }
        return null;

    }
}
