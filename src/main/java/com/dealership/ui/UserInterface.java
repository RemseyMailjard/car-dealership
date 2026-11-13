package com.dealership.ui;

import com.dealership.models.Vehicle;
import com.dealership.persistence.DealershipFileManager;
import com.dealership.services.Dealership;

import java.util.List;
import java.util.Scanner;

/**
 * Handles all console input and output.
 * It never searches or changes the inventory itself; it asks the Dealership to do that.
 */
public class UserInterface {

    private final Scanner scanner = new Scanner(System.in);
    private final DealershipFileManager fileManager = new DealershipFileManager();
    private Dealership dealership;

    /**
     * Starts the application: loads the dealership and keeps showing
     * the menu until the user chooses to quit.
     */
    public void display() {
        if (!init()) {
            return; // inventory could not be loaded; the reason was already printed
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();

            switch (choice) {
                case 1 -> processGetByPriceRequest();
                case 2 -> processGetByMakeModelRequest();
                case 3 -> processGetByYearRequest();
                case 4 -> processGetByColorRequest();
                case 5 -> processGetByMileageRequest();
                case 6 -> processGetByVehicleTypeRequest();
                case 7 -> processGetAllVehiclesRequest();
                case 8 -> processAddVehicleRequest();
                case 9 -> processRemoveVehicleRequest();
                case 99 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please pick an option from the menu.");
            }
        }
    }

    /** Loads the dealership from file. Returns false if that fails. */
    private boolean init() {
        try {
            dealership = fileManager.getDealership();
            System.out.println("Welcome to " + dealership.getName());
            System.out.println(dealership.getAddress() + " | " + dealership.getPhone());
            return true;
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("The application cannot start without an inventory file.");
            return false;
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== CAR DEALERSHIP =====");
        System.out.println();
        System.out.println("1. Find vehicles by price range");
        System.out.println("2. Find vehicles by make/model");
        System.out.println("3. Find vehicles by year range");
        System.out.println("4. Find vehicles by color");
        System.out.println("5. Find vehicles by mileage range");
        System.out.println("6. Find vehicles by type");
        System.out.println("7. List all vehicles");
        System.out.println("8. Add a vehicle");
        System.out.println("9. Remove a vehicle");
        System.out.println();
        System.out.println("99. Quit");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // Menu actions
    // ---------------------------------------------------------------

    private void processGetByPriceRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetByMakeModelRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetByYearRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetByColorRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetByMileageRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetByVehicleTypeRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processGetAllVehiclesRequest() {
        displayVehicles(dealership.getAllVehicles());
    }

    private void processAddVehicleRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    private void processRemoveVehicleRequest() {
        notYetAvailable(); // TODO Phase 5
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Prints a list of vehicles, or a message when the list is empty. */
    private void displayVehicles(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
        System.out.println("(" + vehicles.size() + " vehicle(s) found)");
    }

    private void notYetAvailable() {
        System.out.println("This option is not available yet.");
    }

    /** Keeps asking until the user types a whole number. */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
