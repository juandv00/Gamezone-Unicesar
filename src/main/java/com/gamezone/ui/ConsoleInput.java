package com.gamezone.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Reads and converts the data typed by the user in the console: options,
 * prices, stock, percentages, and dates. Every submenu reads through this
 * single class, so the conversion rules and their messages are not repeated.
 */
public class ConsoleInput {

    private final Scanner scanner;

    /**
     * Creates a reader of the user input.
     *
     * @param scanner the scanner that reads the console
     */
    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads the next line typed by the user, exactly as it was typed.
     *
     * @return the line read
     */
    public String readLine() {
        return scanner.nextLine();
    }

    /**
     * Reads an integer option from the user, returning -1 if the input
     * is not a valid number.
     *
     * @return the option selected by the user
     */
    public int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reads a price from the user, returning null if the input is not a
     * valid non-negative number.
     *
     * @return the price entered, or null if invalid
     */
    public Double readPrice() {
        System.out.print("Precio: ");
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Precio inválido.");
            return null;
        }
    }

    /**
     * Reads a stock quantity from the user, returning null if the input is
     * not a valid integer.
     *
     * @return the stock entered, or null if invalid
     */
    public Integer readStock() {
        System.out.print("Stock: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Stock inválido.");
            return null;
        }
    }

    /**
     * Reads a discount percentage from the user, returning null if the input
     * is not a number between 0 and 100.
     *
     * @return the percentage entered, or null if invalid
     */
    public Double readPercentage() {
        System.out.print("Porcentaje de descuento (0-100): ");
        try {
            double percentage = Double.parseDouble(scanner.nextLine().trim());
            if (percentage < 0 || percentage > 100) {
                System.out.println("El porcentaje debe estar entre 0 y 100.");
                return null;
            }
            return percentage;
        } catch (NumberFormatException e) {
            System.out.println("Porcentaje inválido.");
            return null;
        }
    }

    /**
     * Reads a date in ISO format (YYYY-MM-DD) from the user, returning null
     * if the input is not a valid date.
     *
     * @param prompt the text shown to the user before reading the date
     * @return the date entered, or null if invalid
     */
    public LocalDate readDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida. Use el formato AAAA-MM-DD.");
            return null;
        }
    }

    /**
     * Closes the scanner when the application ends.
     */
    public void close() {
        scanner.close();
    }
}
