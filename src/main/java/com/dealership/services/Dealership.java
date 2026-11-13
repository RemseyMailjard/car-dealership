package com.dealership.services;

import com.dealership.models.Vehicle;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Holds the dealership's details and manages its vehicle inventory.
 *
 * <p>This class contains the application's <em>business logic</em>: every search,
 * every change to the inventory and every validation rule lives here. The user
 * interface only asks this class for results; it never loops over the inventory
 * itself. Because of that, the same logic can be reused by tests, or later by a
 * different interface, without copying any code.
 */
public class Dealership {

    /** The first car (Benz Patent-Motorwagen) is from 1886; nothing older can be in stock. */
    private static final int OLDEST_YEAR = 1886;

    private String name;
    private String address;
    private String phone;

    /**
     * All vehicles currently in stock. The field is {@code final}, so it always points
     * to the same list object, but vehicles can still be added to and removed from it.
     */
    private final List<Vehicle> inventory;

    /**
     * Creates a dealership with an empty inventory. Vehicles are added afterwards
     * with {@link #addVehicle(Vehicle)}.
     *
     * @param name    dealership name
     * @param address street address
     * @param phone   phone number
     */
    public Dealership(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.inventory = new ArrayList<>(); // start with an empty inventory
    }

    // ---------------------------------------------------------------
    // Getters and setters for the dealership details
    // ---------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // ---------------------------------------------------------------
    // Search methods
    // Each one returns a NEW list with the matches (empty if nothing matches),
    // so callers never get access to the inventory list itself.
    // All ranges include both ends: min <= value <= max.
    // ---------------------------------------------------------------

    /**
     * Finds vehicles whose price is between {@code min} and {@code max}.
     *
     * @param min lowest price (inclusive)
     * @param max highest price (inclusive)
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByPrice(BigDecimal min, BigDecimal max) {
        // BigDecimal is an object, so < and > don't work. compareTo() returns
        // a negative number, zero or a positive number (less, equal, greater).
        return filter(v -> v.getPrice().compareTo(min) >= 0 && v.getPrice().compareTo(max) <= 0);
    }

    /**
     * Finds vehicles by make and model, ignoring upper/lower case.
     * If {@code model} is empty, every model of that make is returned.
     *
     * @param make  manufacturer to search for, e.g. "ford"
     * @param model model to search for, or an empty string for any model
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByMakeModel(String make, String model) {
        boolean anyModel = model == null || model.isBlank();
        return filter(v -> v.getMake().equalsIgnoreCase(make.trim())
                && (anyModel || v.getModel().equalsIgnoreCase(model.trim())));
    }

    /**
     * Finds vehicles whose model year is between {@code min} and {@code max}.
     *
     * @param min oldest model year (inclusive)
     * @param max newest model year (inclusive)
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByYear(int min, int max) {
        return filter(v -> v.getYear() >= min && v.getYear() <= max);
    }

    /**
     * Finds vehicles of the given color, ignoring upper/lower case.
     *
     * @param color color to search for, e.g. "red"
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByColor(String color) {
        return filter(v -> v.getColor().equalsIgnoreCase(color.trim()));
    }

    /**
     * Finds vehicles whose odometer reading is between {@code min} and {@code max}.
     *
     * @param min lowest mileage (inclusive)
     * @param max highest mileage (inclusive)
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByMileage(int min, int max) {
        return filter(v -> v.getOdometer() >= min && v.getOdometer() <= max);
    }

    /**
     * Finds vehicles of the given type, ignoring upper/lower case.
     *
     * @param vehicleType type to search for, e.g. "truck"
     * @return matching vehicles, in inventory order
     */
    public List<Vehicle> getVehiclesByType(String vehicleType) {
        return filter(v -> v.getVehicleType().equalsIgnoreCase(vehicleType.trim()));
    }

    /**
     * Looks up one vehicle by its VIN.
     *
     * @param vin the VIN to look for
     * @return the vehicle with this VIN, or {@code null} if it is not in the inventory
     */
    public Vehicle getVehicleByVin(int vin) {
        for (Vehicle v : inventory) {
            if (v.getVin() == vin) {
                return v;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Inventory methods
    // ---------------------------------------------------------------

    /**
     * Returns every vehicle in stock.
     *
     * <p>This returns a <em>copy</em> of the inventory list. If it returned the real
     * list, any other class could call {@code clear()} or {@code add()} on it and
     * skip the duplicate and validation checks in {@link #addVehicle(Vehicle)}.
     *
     * @return a new list containing all vehicles, in inventory order
     */
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(inventory);
    }

    /**
     * Adds a vehicle to the inventory after checking it.
     *
     * <p>Two kinds of problems are reported differently:
     * <ul>
     *   <li>a duplicate VIN (or {@code null}) is an expected situation, so the method
     *       simply returns {@code false};</li>
     *   <li>invalid data (for example a negative price) throws an exception whose
     *       message tells the user exactly what is wrong.</li>
     * </ul>
     *
     * @param vehicle the vehicle to add
     * @return {@code true} if added; {@code false} if the vehicle is {@code null}
     *         or its VIN is already in stock
     * @throws IllegalArgumentException if the vehicle breaks a business rule
     */
    public boolean addVehicle(Vehicle vehicle) {
        if (vehicle == null || getVehicleByVin(vehicle.getVin()) != null) {
            return false;
        }
        validate(vehicle);
        inventory.add(vehicle);
        return true;
    }

    /**
     * Removes a vehicle from the inventory. Vehicles are matched by VIN, so the
     * argument does not have to be the exact same object that is in the list.
     *
     * @param vehicle the vehicle to remove
     * @return {@code true} if a vehicle was removed; {@code false} if it was not in stock
     */
    public boolean removeVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        Vehicle inStock = getVehicleByVin(vehicle.getVin());
        return inStock != null && inventory.remove(inStock);
    }

    // ---------------------------------------------------------------
    // Private helpers (only used inside this class)
    // ---------------------------------------------------------------

    /**
     * Returns every vehicle that matches the given condition.
     *
     * <p>All six search methods use this helper, so the search loop is written only
     * once. Each search passes its own condition as a <em>lambda</em>: a short,
     * unnamed function. For example {@code v -> v.getYear() >= 2000} means
     * "given a vehicle v, return true if its year is 2000 or later".
     * {@link Predicate} is the Java type for such a true/false function.
     *
     * @param condition returns {@code true} for the vehicles that should be kept
     * @return a new list with the matching vehicles
     */
    private List<Vehicle> filter(Predicate<Vehicle> condition) {
        List<Vehicle> matches = new ArrayList<>();
        for (Vehicle v : inventory) {
            if (condition.test(v)) {
                matches.add(v);
            }
        }
        return matches;
    }

    /**
     * Checks all business rules for a vehicle and stops at the first problem.
     * This is the single place where these rules are defined; both the user
     * interface and the file loader rely on it.
     *
     * @param v the vehicle to check
     * @throws IllegalArgumentException with a user-friendly message if a rule is broken
     */
    private void validate(Vehicle v) {
        if (v.getVin() <= 0) {
            throw new IllegalArgumentException("VIN must be a positive number.");
        }
        int maxYear = Year.now().getValue() + 1; // next year's models are already sold
        if (v.getYear() < OLDEST_YEAR || v.getYear() > maxYear) {
            throw new IllegalArgumentException("Year must be between " + OLDEST_YEAR + " and " + maxYear + ".");
        }
        requireText(v.getMake(), "Make");
        requireText(v.getModel(), "Model");
        requireText(v.getVehicleType(), "Vehicle type");
        requireText(v.getColor(), "Color");
        if (v.getOdometer() < 0) {
            throw new IllegalArgumentException("Odometer cannot be negative.");
        }
        if (v.getPrice() == null || v.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
    }

    /**
     * Checks one text field. It must not be empty, and it must not contain '|',
     * because '|' separates the columns in the inventory file; a '|' inside a
     * value would split it into two columns and corrupt the file.
     *
     * @param value     the text to check
     * @param fieldName name used in the error message, e.g. "Make"
     * @throws IllegalArgumentException if the text is empty or contains '|'
     */
    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        if (value.contains("|")) {
            throw new IllegalArgumentException(fieldName + " cannot contain the '|' character.");
        }
    }
}
