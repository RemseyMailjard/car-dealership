package com.dealership.ui;

import com.dealership.models.Vehicle;
import com.dealership.persistence.DealershipFileManager;
import com.dealership.services.Dealership;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Handles all interaction with the user through the console.
 *
 * <p>This is the only class that reads from the keyboard or prints to the screen.
 * It never searches or changes the inventory itself: it reads the user's input,
 * asks the {@link Dealership} for a result, and shows that result. Keeping input
 * and output separate from the business logic is called <em>separation of concerns</em>.
 *
 * <p>Every menu option has its own {@code process...Request()} method, so each
 * method stays short and does one thing.
 */
public class UserInterface {

    /** Reads keyboard input. One Scanner is shared, because System.in should only be wrapped once. */
    private final Scanner scanner = new Scanner(System.in);
    /** Loads the dealership at startup and saves it after every change. */
    private final DealershipFileManager fileManager = new DealershipFileManager();
    /** The dealership being managed; set by {@link #init()}. */
    private Dealership dealership;

    /**
     * Starts the application: loads the dealership, then keeps showing the menu
     * until the user chooses 99 (Quit) or the input ends.
     */
    public void display() {
        if (!init()) {
            return; // inventory could not be loaded; the reason was already printed
        }

        try {
            runMenuLoop();
        } catch (NoSuchElementException e) {
            // Scanner throws this when there is no more input, e.g. when the user
            // presses Ctrl+D (Mac/Linux) or Ctrl+Z (Windows). Exit politely instead of crashing.
            System.out.println();
            System.out.println("Input ended. Goodbye!");
        }
    }

    /** Shows the menu and handles choices until the user picks 99. */
    private void runMenuLoop() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();

            // "case 1 ->" is the modern switch syntax (Java 14+): no "break" needed,
            // because only the code after the arrow runs.
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
                    running = false; // ends the while loop
                }
                default -> System.out.println("Invalid choice. Please pick an option from the menu.");
            }
        }
    }

    /**
     * Loads the dealership from the inventory file and shows a welcome message.
     *
     * @return {@code true} if loading worked; {@code false} if the app cannot continue
     */
    private boolean init() {
        try {
            dealership = fileManager.getDealership();
            System.out.println("Welcome to " + dealership.getName());
            System.out.println(dealership.getAddress() + " | " + dealership.getPhone());
            return true;
        } catch (IllegalStateException e) {
            // Show a clear message instead of a stack trace
            System.out.println("Error: " + e.getMessage());
            System.out.println("The application cannot start without an inventory file.");
            return false;
        }
    }

    /** Prints the main menu. */
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
    // Each one follows the same three steps:
    //   1. read the input   2. ask the Dealership   3. show the result
    // ---------------------------------------------------------------

    /** Menu option 1: asks for a price range and shows the vehicles in it. */
    private void processGetByPriceRequest() {
        BigDecimal min = readPrice("Minimum price: ");
        BigDecimal max = readPrice("Maximum price: ");
        if (max.compareTo(min) < 0) {
            System.out.println("Maximum price cannot be lower than minimum price.");
            return;
        }
        displayVehicles(dealership.getVehiclesByPrice(min, max));
    }

    /** Menu option 2: asks for a make and an optional model and shows the matches. */
    private void processGetByMakeModelRequest() {
        String make = readText("Make: ");
        // Model may be left empty, so readText() (which rejects empty input) is not used here
        System.out.print("Model (press Enter for any model): ");
        String model = scanner.nextLine().trim();
        displayVehicles(dealership.getVehiclesByMakeModel(make, model));
    }

    /** Menu option 3: asks for a year range and shows the vehicles in it. */
    private void processGetByYearRequest() {
        int min = readInt("Minimum year: ");
        int max = readInt("Maximum year: ");
        if (max < min) {
            System.out.println("Maximum year cannot be lower than minimum year.");
            return;
        }
        displayVehicles(dealership.getVehiclesByYear(min, max));
    }

    /** Menu option 4: asks for a color and shows the vehicles with that color. */
    private void processGetByColorRequest() {
        displayVehicles(dealership.getVehiclesByColor(readText("Color: ")));
    }

    /** Menu option 5: asks for a mileage range and shows the vehicles in it. */
    private void processGetByMileageRequest() {
        int min = readInt("Minimum mileage: ");
        int max = readInt("Maximum mileage: ");
        if (max < min) {
            System.out.println("Maximum mileage cannot be lower than minimum mileage.");
            return;
        }
        displayVehicles(dealership.getVehiclesByMileage(min, max));
    }

    /** Menu option 6: asks for a vehicle type and shows the vehicles of that type. */
    private void processGetByVehicleTypeRequest() {
        displayVehicles(dealership.getVehiclesByType(readText("Vehicle type (car, truck, SUV, van): ")));
    }

    /** Menu option 7: shows every vehicle in stock. */
    private void processGetAllVehiclesRequest() {
        displayVehicles(dealership.getAllVehicles());
    }

    /**
     * Menu option 8: asks for all details of a new vehicle, adds it, and saves.
     * The UI only checks that numbers are numbers; the business rules
     * (valid year, positive price, ...) are checked by {@link Dealership#addVehicle(Vehicle)}.
     */
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
            if (dealership.addVehicle(vehicle)) {
                System.out.println("Vehicle added:");
                System.out.println(vehicle);
                save();
            } else {
                System.out.println("A vehicle with VIN " + vin + " already exists.");
            }
        } catch (IllegalArgumentException e) {
            // The exception message says which rule was broken, e.g. "Price must be greater than 0."
            System.out.println("Vehicle not added: " + e.getMessage());
        }
    }

    /**
     * Menu option 9: asks for a VIN, shows that vehicle, and removes it after
     * the user confirms. Removing cannot be undone, hence the confirmation.
     */
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
    // Helpers, shared by the menu actions above so that code for
    // showing results and reading input is not repeated.
    // ---------------------------------------------------------------

    /**
     * Prints each vehicle on its own line (using {@link Vehicle#toString()}),
     * followed by the number of results, or a message if the list is empty.
     *
     * @param vehicles the vehicles to show
     */
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

    /**
     * Saves the inventory to the file. If saving fails, the user gets a warning,
     * but the change is kept in memory for the rest of this session.
     */
    private void save() {
        try {
            fileManager.saveDealership(dealership);
            System.out.println("Inventory saved.");
        } catch (IllegalStateException e) {
            System.out.println("Warning: " + e.getMessage());
        }
    }

    /*
     * About reading input: every helper below uses scanner.nextLine() and then converts
     * the text. Mixing nextInt() with nextLine() is a classic bug: nextInt() leaves the
     * Enter key in the buffer, so the next nextLine() returns an empty string immediately.
     */

    /**
     * Asks a question until the user types a whole number.
     *
     * @param prompt the question to show
     * @return the number the user typed
     */
    private int readInt(String prompt) {
        while (true) { // repeat until a valid number is returned
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    /**
     * Asks a question until the user types a valid amount of money, such as
     * {@code 1995} or {@code 1995.00}. A leading "$" is allowed; negative amounts are not.
     *
     * @param prompt the question to show
     * @return the amount the user typed
     */
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

    /**
     * Asks a question until the user types something that is not empty.
     *
     * @param prompt the question to show
     * @return the text the user typed, without surrounding spaces
     */
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
