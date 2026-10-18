package com.gamezone.ui;

/**
 * A submenu of the console user interface. The main menu lists every
 * {@code Menu} with its title and opens the one the user chooses, so a new
 * module is added by creating a new class that implements this interface
 * and passing it to {@link ConsoleUI}, without changing {@link ConsoleUI}.
 */
public interface Menu {

    /**
     * Returns the title of the option of this submenu in the main menu.
     *
     * @return the title of the option, for example "Gestión de productos"
     */
    String getTitle();

    /**
     * Displays this submenu and runs the operation chosen by the user until
     * the user goes back to the main menu.
     */
    void show();
}
