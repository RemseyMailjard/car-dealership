package com.dealership.ui;

import com.dealership.models.Vehicle;
import com.dealership.persistence.DealershipFileManager;
import com.dealership.services.Dealership;

import java.math.BigDecimal;
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
    // Menu actions: read input, ask the Dealership, show the result
    // ---------------------------------------------------------------

    private void processGetByPriceRequest() {
        BigDecimal min = readPrice("Minimum price: ");
        BigDecimal max = readPrice("Maximum price: ");
        if (max.compareTo(min) < 0) {
            System.out.println("Maximum price cannot be lower than minimum price.");
            return;
        }
        displayVehicles(dealership.getVehiclesByPrice(min, max));
    }

    private void processGetByMakeModelRequest() {
        String make = readText("Make: ");
        System.out.print("Model (press Enter for any model): ");
        String model = scanner.nextLine().trim();
        displayVehicles(dealership.getVehiclesByMakeModel(make, model));
    }

    private void processGetByYearRequest() {
        int min = readInt("Minimum year: ");
        int max = readInt("Maximum year: ");
        if (max < min) {
            System.out.println("Maximum year cannot be lower than minimum year.");
            return;
        }
        displayVehicles(dealership.getVehiclesByYear(min, max));
    }

    private void processGetByColorRequest() {
        displayVehicles(dealership.getVehiclesByColor(readText("Color: ")));
    }

    private void processGetByMileageRequest() {
        int min = readInt("Minimum mileage: ");
        int max = readInt("Maximum mileage: ");
        if (max < min) {
            System.out.println("Maximum mileage cannot be lower than minimum mileage.");
            return;
        }
        displayVehicles(dealership.getVehiclesByMileage(min, max));
    }

    private void processGetByVehicleTypeRequest() {
        displayVehicles(dealership.getVehiclesByType(readText("Vehicle type (car, truck, SUV, van): ")));
    }

    private void processGetAllVehiclesRequest() {
        displayVehicles(dealership.getAllVehicles());
    }

    private void processAddVehicleRequest() {
        System.out.println("--- Add a vehicle ---");
        int vin = readInt("VIN: ");
        if (dealership.getVehicleByVin(vin) != null) {
            // Check right away, so the user doesn't type everything else for nothing
            System.out.println("A vehicle with VIN " + vin + " already exists.");
            return;
        }
        int year = readInt("Year: ");
        String make = readText("Make: ");
        String model = readText("Model: ");
        String vehicleType = readText("Vehicle type (car, truck, SUV, van): ");
        String color = readText("Color: ");
        int odometer = readInt("Odometer (miles): ");
        BigDecimal price = readPrice("Price: ");

        Vehicle vehicle = new Vehicle(vin, year, make, model, vehicleType, color, odometer, price);
        try {
            // The Dealership checks the business rules (year range, no negative values, ...)
            if (dealership.addVehicle(vehicle)) {
                System.out.println("Vehicle added:");
                System.out.println(vehicle);
                save();
            } else {
                System.out.println("A vehicle with VIN " + vin + " already exists.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Vehicle not added: " + e.getMessage());
        }
    }

    private void processRemoveVehicleRequest() {
        int vin = readInt("VIN of the vehicle to remove: ");
        Vehicle vehicle = dealership.getVehicleByVin(vin);
        if (vehicle == null) {
            System.out.println("No vehicle found with VIN " + vin + ".");
            return;
        }

        System.out.println(vehicle);
        System.out.print("Remove this vehicle? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Nothing removed.");
            return;
        }

        if (dealership.removeVehicle(vehicle)) {
            System.out.println("Vehicle removed.");
            save();
        }
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

    /** Writes the inventory to file. The change stays in memory even if saving fails. */
    private void save() {
        try {
            fileManager.saveDealership(dealership);
            System.out.println("Inventory saved.");
        } catch (IllegalStateException e) {
            System.out.println("Warning: " + e.getMessage());
        }
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

    /** Keeps asking until the user types a valid amount, such as 1995 or 1995.00 (no negatives). */
    private BigDecimal readPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            // Commas are NOT removed: "1995,00" would silently become 199500
            String input = scanner.nextLine().trim().replace("$", "");
            try {
                BigDecimal amount = new BigDecimal(input);
                if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                    return amount;
                }
                System.out.println("Amount cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter an amount, for example 1995.00");
            }
        }
    }

    /** Keeps asking until the user types something that is not empty. */
    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }
}
