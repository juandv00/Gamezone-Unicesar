package com.gamezone.ui;

import com.gamezone.model.Client;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;
import java.util.List;

/**
 * Console submenu of the people module: registers clients and lists the
 * clients and the sellers. Every operation runs through the
 * {@link ErrorHandler}, so its errors are shown as clear messages.
 */
public class PersonMenu implements Menu {

    private final PersonService personService;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param personService the service used for people operations
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public PersonMenu(PersonService personService,
                      ConsoleInput input, ErrorHandler errorHandler) {
        this.personService = personService;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Returns the title of the option of this submenu in the main menu.
     *
     * @return the title of the option
     */
    @Override
    public String getTitle() {
        return "Gestión de personas";
    }

    /**
     * Displays the people management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    @Override
    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de personas ---");
            System.out.println("1. Registrar un cliente");
            System.out.println("2. Listar todos los clientes");
            System.out.println("3. Listar todos los vendedores");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerClient);
                case 2 -> errorHandler.executeOperation(this::listClients);
                case 3 -> errorHandler.executeOperation(this::listSellers);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new client and registers it.
     */
    private void registerClient() {
        System.out.print("Identificación del cliente: ");
        String id = input.readLine().trim();
        System.out.print("Nombre: ");
        String name = input.readLine().trim();
        System.out.print("Teléfono: ");
        String phone = input.readLine().trim();
        System.out.print("Correo electrónico: ");
        String email = input.readLine().trim();

        personService.registerClient(id, name, phone, email);
        System.out.println("Cliente registrado correctamente.");
    }

    /**
     * Lists every client currently registered.
     */
    private void listClients() {
        List<Client> clients = personService.listClient();
        if (clients.isEmpty()) {
            System.out.println("Aún no hay clientes registrados.");
            return;
        }
        System.out.println("\n--- Clientes registrados ---");
        for (Client client : clients) {
            System.out.println(client);
        }
    }

    /**
     * Lists every seller currently registered.
     */
    private void listSellers() {
        List<Seller> sellers = personService.listSeller();
        if (sellers.isEmpty()) {
            System.out.println("Aún no hay vendedores registrados.");
            return;
        }
        System.out.println("\n--- Vendedores registrados ---");
        for (Seller seller : sellers) {
            System.out.println(seller);
        }
    }
}
