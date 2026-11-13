package com.dealership.models;

import java.math.BigDecimal;

/**
 * Represents a single vehicle in the dealership's inventory.
 *
 * <p>This is a plain data class (a "model"): it only stores the details of one car
 * and has no business logic. Checking whether a vehicle is valid is the job of
 * {@link com.dealership.services.Dealership}, which keeps all rules in one place.
 *
 * <p>All fields are {@code private} and can only be reached through getters and
 * setters. This is called <em>encapsulation</em>: other classes cannot change a
 * vehicle's data directly, only through the methods this class offers.
 */
public class Vehicle {

    /** Vehicle Identification Number; unique within one dealership. */
    private int vin;
    /** Model year, e.g. 2012. */
    private int year;
    /** Manufacturer, e.g. "Ford". */
    private String make;
    /** Model name, e.g. "Explorer". */
    private String model;
    /** Kind of vehicle, e.g. "car", "truck", "SUV" or "van". */
    private String vehicleType;
    private String color;
    /** Mileage in miles. */
    private int odometer;
    /**
     * Asking price in dollars. {@link BigDecimal} is used instead of {@code double}
     * because a {@code double} cannot store most decimal amounts exactly
     * (0.1 + 0.2 gives 0.30000000000000004), which is unacceptable for money.
     */
    private BigDecimal price;

    /**
     * Creates a vehicle with all of its details.
     *
     * @param vin         unique vehicle identification number
     * @param year        model year
     * @param make        manufacturer
     * @param model       model name
     * @param vehicleType kind of vehicle (car, truck, SUV, van, ...)
     * @param color       exterior color
     * @param odometer    mileage in miles
     * @param price       asking price in dollars
     */
    public Vehicle(int vin, int year, String make, String model, String vehicleType,
                   String color, int odometer, BigDecimal price) {
        // "this.vin" is the field; "vin" on its own is the constructor parameter
        this.vin = vin;
        this.year = year;
        this.make = make;
        this.model = model;
        this.vehicleType = vehicleType;
        this.color = color;
        this.odometer = odometer;
        this.price = price;
    }

    // ---------------------------------------------------------------
    // Getters and setters: the only way other classes can read or
    // change a vehicle's data (see "encapsulation" above).
    // ---------------------------------------------------------------

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
     * Returns the vehicle as one line of text. Java calls this method automatically
     * when a vehicle is printed, e.g. {@code System.out.println(vehicle)}.
     *
     * <p>Every column has a fixed width, so a list of vehicles lines up like a table:
     * <pre>
     * VIN: 10112  | 1993 Ford       Explorer     | SUV      | Red      |   525,123 mi | $      995.00
     * </pre>
     * Format codes used: {@code %-10s} = text, left-aligned in 10 characters;
     * {@code %,9d} = whole number with thousands separators, 9 characters wide;
     * {@code %,12.2f} = number with 2 decimals and thousands separators.
     *
     * @return a readable, aligned description of this vehicle
     */
    @Override
    public String toString() {
        return String.format("VIN: %-6d | %4d %-10s %-12s | %-8s | %-8s | %,9d mi | $%,12.2f",
                vin, year, make, model, vehicleType, color, odometer, price);
    }
}
