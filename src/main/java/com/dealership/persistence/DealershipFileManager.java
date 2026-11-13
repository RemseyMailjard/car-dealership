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
 * Loads the dealership from the inventory file, and saves it back.
 *
 * <p>This is the only class that knows how the file is formatted. If the format
 * ever changes (for example to JSON or a database), only this class has to change;
 * {@link Dealership} and the user interface stay the same.
 *
 * <p>File format: values are separated by a pipe character ({@code |}).
 * <pre>
 * line 1:  name|address|phone
 * line 2+: vin|year|make|model|type|color|odometer|price
 * </pre>
 */
public class DealershipFileManager {

    /** Relative path, so it is resolved from the folder the program is started in. */
    private static final String DEFAULT_FILE = "data/inventory.csv";

    /**
     * {@link String#split(String)} expects a regular expression, in which '|' means
     * "or". The backslashes turn it into a normal '|' character.
     */
    private static final String DELIMITER = "\\|";

    private final String filePath;

    /** Creates a file manager for the default inventory file, {@code data/inventory.csv}. */
    public DealershipFileManager() {
        this(DEFAULT_FILE); // calls the other constructor below
    }

    /**
     * Creates a file manager for a specific file. The tests use this so they can
     * work on a temporary file and never touch the real inventory.
     *
     * @param filePath path of the inventory file to read and write
     */
    public DealershipFileManager(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the file and builds a {@link Dealership} with all its vehicles.
     *
     * <p>A vehicle line that cannot be used (wrong number of fields, text where a
     * number should be, invalid data or a duplicate VIN) is skipped with a warning.
     * That way one bad line does not stop the rest of the inventory from loading.
     *
     * @return the dealership, with every valid vehicle from the file
     * @throws IllegalStateException if the file cannot be read, is empty,
     *                               or its first line is not a valid dealership line
     */
    public Dealership getDealership() {
        // "try-with-resources": the reader is closed automatically when this block
        // ends, even if an exception is thrown.
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalStateException("Inventory file is empty: " + filePath);
            }
            Dealership dealership = parseDealership(headerLine);

            // readLine() returns null when the end of the file is reached
            String line;
            int lineNumber = 1; // used in warnings, so the user can find the bad line
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue; // ignore empty lines, e.g. at the end of the file
                }
                try {
                    Vehicle vehicle = parseVehicle(line);
                    // addVehicle also runs the business-rule checks from Dealership
                    if (!dealership.addVehicle(vehicle)) {
                        System.err.println("Warning: duplicate VIN on line " + lineNumber + ", skipped.");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: invalid vehicle on line " + lineNumber + " skipped (" + e.getMessage() + ")");
                }
            }
            return dealership;
        } catch (IOException e) {
            // IOException is a "checked" exception that every caller would have to handle.
            // Wrapping it keeps the method signature simple for the user interface.
            throw new IllegalStateException("Could not read inventory file: " + filePath, e);
        }
    }

    /**
     * Writes the dealership details and its current inventory to the file,
     * replacing everything that was in it before.
     *
     * @param dealership the dealership to save
     * @throws IllegalStateException if the file cannot be written
     */
    public void saveDealership(Dealership dealership) {
        // FileWriter without "append" overwrites the file from the start
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.join("|", dealership.getName(), dealership.getAddress(), dealership.getPhone()));
            writer.newLine();

            for (Vehicle v : dealership.getAllVehicles()) {
                // Same column order as parseVehicle() reads them
                writer.write(v.getVin() + "|" + v.getYear() + "|" + v.getMake() + "|" + v.getModel() + "|"
                        + v.getVehicleType() + "|" + v.getColor() + "|" + v.getOdometer() + "|"
                        + formatPrice(v.getPrice()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save inventory file: " + filePath, e);
        }
    }

    /**
     * Formats a price with exactly two decimals, e.g. 995 becomes "995.00".
     * {@code toPlainString()} avoids scientific notation such as "1E+3".
     */
    private String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Turns the first line of the file ({@code name|address|phone}) into a Dealership.
     *
     * @throws IllegalStateException if the line does not have exactly 3 fields
     */
    private Dealership parseDealership(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid dealership line: " + line);
        }
        return new Dealership(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }

    /**
     * Turns one vehicle line ({@code vin|year|make|model|type|color|odometer|price})
     * into a Vehicle. Spaces around each value are removed with {@code trim()}.
     *
     * @throws IllegalArgumentException if the line does not have 8 fields or a number
     *         cannot be read. ({@code NumberFormatException}, thrown by
     *         {@code Integer.parseInt} and {@code new BigDecimal}, is a subclass of
     *         {@code IllegalArgumentException}, so one catch handles both.)
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
