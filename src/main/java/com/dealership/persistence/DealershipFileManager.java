package com.dealership.persistence;

import com.dealership.models.Vehicle;
import com.dealership.services.Dealership;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Loads and saves the dealership and its inventory from/to a pipe-delimited CSV file.
 *
 * File format:
 *   line 1:  name|address|phone
 *   line 2+: vin|year|make|model|type|color|odometer|price
 */
public class DealershipFileManager {

    private static final String DEFAULT_FILE = "data/inventory.csv";
    private static final String DELIMITER = "\\|"; // '|' is special in regex, so it is escaped

    private final String filePath;

    /** Uses the default inventory file: data/inventory.csv */
    public DealershipFileManager() {
        this(DEFAULT_FILE);
    }

    /** Uses a custom file path (handy for tests). */
    public DealershipFileManager(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the file and builds a Dealership with all its vehicles.
     * Lines that cannot be parsed are skipped with a warning, so one bad
     * line does not prevent the rest of the inventory from loading.
     *
     * @throws IllegalStateException if the file cannot be read or has no dealership line
     */
    public Dealership getDealership() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalStateException("Inventory file is empty: " + filePath);
            }
            Dealership dealership = parseDealership(headerLine);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Vehicle vehicle = parseVehicle(line);
                    if (!dealership.addVehicle(vehicle)) {
                        System.err.println("Warning: duplicate VIN on line " + lineNumber + ", skipped.");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: invalid vehicle on line " + lineNumber + " skipped (" + e.getMessage() + ")");
                }
            }
            return dealership;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read inventory file: " + filePath, e);
        }
    }

    /**
     * Overwrites the file with the dealership info and its current inventory.
     *
     * @throws IllegalStateException if the file cannot be written
     */
    public void saveDealership(Dealership dealership) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.join("|", dealership.getName(), dealership.getAddress(), dealership.getPhone()));
            writer.newLine();

            for (Vehicle v : dealership.getAllVehicles()) {
                writer.write(v.getVin() + "|" + v.getYear() + "|" + v.getMake() + "|" + v.getModel() + "|"
                        + v.getVehicleType() + "|" + v.getColor() + "|" + v.getOdometer() + "|"
                        + formatPrice(v.getPrice()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save inventory file: " + filePath, e);
        }
    }

    /** Always writes prices with two decimals, e.g. 995 becomes 995.00 */
    private String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Parses the first line: name|address|phone */
    private Dealership parseDealership(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid dealership line: " + line);
        }
        return new Dealership(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }

    /**
     * Parses one vehicle line: vin|year|make|model|type|color|odometer|price
     *
     * @throws IllegalArgumentException if the line has the wrong number of fields or bad numbers
     *         (NumberFormatException is a subclass of IllegalArgumentException)
     */
    private Vehicle parseVehicle(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 8) {
            throw new IllegalArgumentException("expected 8 fields but found " + parts.length);
        }
        int vin = Integer.parseInt(parts[0].trim());
        int year = Integer.parseInt(parts[1].trim());
        String make = parts[2].trim();
        String model = parts[3].trim();
        String vehicleType = parts[4].trim();
        String color = parts[5].trim();
        int odometer = Integer.parseInt(parts[6].trim());
        BigDecimal price = new BigDecimal(parts[7].trim());

        return new Vehicle(vin, year, make, model, vehicleType, color, odometer, price);
    }
}
