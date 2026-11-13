package com.dealership.services;

import com.dealership.models.Vehicle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds dealership information and manages its vehicle inventory.
 * All searching and inventory changes happen here, never in the user interface.
 */
public class Dealership {

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
    // Search methods (placeholders for now; implemented in Phase 5)
    // ---------------------------------------------------------------

    public List<Vehicle> getVehiclesByPrice(BigDecimal min, BigDecimal max) {
        // TODO Phase 5: return vehicles with min <= price <= max
        return new ArrayList<>();
    }

    public List<Vehicle> getVehiclesByMakeModel(String make, String model) {
        // TODO Phase 5: case-insensitive match on make and model
        return new ArrayList<>();
    }

    public List<Vehicle> getVehiclesByYear(int min, int max) {
        // TODO Phase 5: return vehicles with min <= year <= max
        return new ArrayList<>();
    }

    public List<Vehicle> getVehiclesByColor(String color) {
        // TODO Phase 5: case-insensitive match on color
        return new ArrayList<>();
    }

    public List<Vehicle> getVehiclesByMileage(int min, int max) {
        // TODO Phase 5: return vehicles with min <= odometer <= max
        return new ArrayList<>();
    }

    public List<Vehicle> getVehiclesByType(String vehicleType) {
        // TODO Phase 5: case-insensitive match on vehicle type
        return new ArrayList<>();
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
     */
    public boolean addVehicle(Vehicle vehicle) {
        if (vehicle == null || containsVin(vehicle.getVin())) {
            return false;
        }
        inventory.add(vehicle);
        return true;
    }

    public boolean removeVehicle(Vehicle vehicle) {
        // TODO Phase 5: remove the vehicle from the inventory
        return false;
    }

    /** Checks whether a vehicle with the given VIN is already in the inventory. */
    private boolean containsVin(int vin) {
        for (Vehicle v : inventory) {
            if (v.getVin() == vin) {
                return true;
            }
        }
        return false;
    }
}
