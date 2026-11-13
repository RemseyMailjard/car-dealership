package com.dealership.models;

import java.math.BigDecimal;

/**
 * Represents a single vehicle in the dealership's inventory.
 * This is a plain data class: it holds vehicle details and nothing else.
 */
public class Vehicle {

    private int vin;
    private int year;
    private String make;
    private String model;
    private String vehicleType;
    private String color;
    private int odometer;
    private BigDecimal price; // BigDecimal avoids rounding errors with money

    public Vehicle(int vin, int year, String make, String model, String vehicleType,
                   String color, int odometer, BigDecimal price) {
        this.vin = vin;
        this.year = year;
        this.make = make;
        this.model = model;
        this.vehicleType = vehicleType;
        this.color = color;
        this.odometer = odometer;
        this.price = price;
    }

    public int getVin() {
        return vin;
    }

    public void setVin(int vin) {
        this.vin = vin;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getOdometer() {
        return odometer;
    }

    public void setOdometer(int odometer) {
        this.odometer = odometer;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns one aligned line per vehicle, so a list of vehicles reads like a table.
     */
    @Override
    public String toString() {
        return String.format("VIN: %-6d | %4d %-10s %-12s | %-8s | %-8s | %,9d mi | $%,12.2f",
                vin, year, make, model, vehicleType, color, odometer, price);
    }
}
