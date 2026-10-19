package com.gamezone.ui;

import java.util.List;

/**
 * Provides the console-based user interface for GameZone Unicesar. This
 * class only shows the main menu and opens the submenu chosen by the user.
 * The submenus are received as a list of {@link Menu} objects, so the main
 * menu is built from their titles and a new module is added by passing a new
 * {@link Menu} to the constructor, without changing this class. The
 * submenus read the user input through {@link ConsoleInput} and run every
 * operation through {@link ErrorHandler}, which shows a different message
 * for each category of error, so the user never sees a technical message or
 * a stack trace.
 */
public class ConsoleUI {

    private final ConsoleInput input;
    private final List<Menu> menus;

    /**
     * Creates a new ConsoleUI with the reader of the user input and the
     * submenus of the main menu, in the order in which they are listed.
     *
     * @param input the reader of the user input
     * @param menus the submenus of the main menu; option 1 opens the first one
     */
    public ConsoleUI(ConsoleInput input, List<Menu> menus) {
        this.input = input;
        this.menus = List.copyOf(menus);
    }

    /**
     * Starts the application's main loop, showing the main menu until
     * the user chooses to exit.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            int option = input.readOption();
            if (option == 0) {
                exit = true;
            } else if (option >= 1 && option <= menus.size()) {
                menus.get(option - 1).show();
            } else {
                System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
        System.out.println("¡Gracias por usar GameZone Unicesar!");
        input.close();
    }

    /**
     * Displays the main menu: one numbered option for each submenu, with its
     * title, and the option to exit.
     */
    private void showMainMenu() {
        System.out.println("\n===== GameZone Unicesar =====");
        for (int i = 0; i < menus.size(); i++) {
            System.out.println((i + 1) + ". " + menus.get(i).getTitle());
        }
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }
}
