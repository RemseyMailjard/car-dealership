package com.dealership.services;

import com.dealership.models.Vehicle;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Holds dealership information and manages its vehicle inventory.
 * All searching and inventory changes happen here, never in the user interface.
 */
public class Dealership {

    /** The first car (Benz Patent-Motorwagen) is from 1886; nothing older can be in stock. */
    private static final int OLDEST_YEAR = 1886;

    private String name;
    private String address;
    private String phone;
    private final List<Vehicle> inventory;

    public Dealership(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.inventory = new ArrayList<>(); // start with an empty inventory
    }

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
    // Search methods (all ranges include both min and max)
    // ---------------------------------------------------------------

    public List<Vehicle> getVehiclesByPrice(BigDecimal min, BigDecimal max) {
        // BigDecimal must be compared with compareTo(), not with < or >
        return filter(v -> v.getPrice().compareTo(min) >= 0 && v.getPrice().compareTo(max) <= 0);
    }

    /**
     * Finds vehicles by make and model (case-insensitive).
     * If model is empty, all models of that make are returned.
     */
    public List<Vehicle> getVehiclesByMakeModel(String make, String model) {
        boolean anyModel = model == null || model.isBlank();
        return filter(v -> v.getMake().equalsIgnoreCase(make.trim())
                && (anyModel || v.getModel().equalsIgnoreCase(model.trim())));
    }

    public List<Vehicle> getVehiclesByYear(int min, int max) {
        return filter(v -> v.getYear() >= min && v.getYear() <= max);
    }

    public List<Vehicle> getVehiclesByColor(String color) {
        return filter(v -> v.getColor().equalsIgnoreCase(color.trim()));
    }

    public List<Vehicle> getVehiclesByMileage(int min, int max) {
        return filter(v -> v.getOdometer() >= min && v.getOdometer() <= max);
    }

    public List<Vehicle> getVehiclesByType(String vehicleType) {
        return filter(v -> v.getVehicleType().equalsIgnoreCase(vehicleType.trim()));
    }

    /** Returns the vehicle with this VIN, or null if it is not in the inventory. */
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
     * Returns a copy of the inventory, so callers cannot change
     * the dealership's list without going through this class.
     */
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(inventory);
    }

    /**
     * Adds a vehicle to the inventory.
     *
     * @return true if added; false if the vehicle is null or its VIN is already in stock
     * @throws IllegalArgumentException if the vehicle has invalid data (message says what is wrong)
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
     * Removes the vehicle with the same VIN as the given vehicle.
     *
     * @return true if a vehicle was removed; false if it was not in the inventory
     */
    public boolean removeVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        Vehicle inStock = getVehicleByVin(vehicle.getVin());
        return inStock != null && inventory.remove(inStock);
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Returns every vehicle that matches the given condition.
     * All search methods use this, so the loop is written only once.
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

    /** Checks all business rules for a vehicle. Throws with a clear message on the first problem. */
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

    /** Text fields must not be empty and must not contain '|', because that is the CSV separator. */
    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        if (value.contains("|")) {
            throw new IllegalArgumentException(fieldName + " cannot contain the '|' character.");
        }
    }
}
