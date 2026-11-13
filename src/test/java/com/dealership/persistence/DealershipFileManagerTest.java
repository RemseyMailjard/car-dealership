package com.dealership.persistence;

import com.dealership.models.Vehicle;
import com.dealership.services.Dealership;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DealershipFileManagerTest {

    @TempDir
    Path tempDir;

    /** Writes the given lines to a temporary inventory file and returns a manager for it. */
    private DealershipFileManager managerFor(String... lines) throws IOException {
        Path file = tempDir.resolve("inventory.csv");
        Files.write(file, List.of(lines));
        return new DealershipFileManager(file.toString());
    }

    @Test
    void loadsDealershipInfoAndVehicles() throws IOException {
        DealershipFileManager manager = managerFor(
                "D & B Used Cars|111 Old Benbrook Rd|817-555-5555",
                "10112|1993|Ford|Explorer|SUV|Red|525123|995.00",
                "44901|2012|Honda|Civic|car|Gray|103221|6995.00");

        Dealership dealership = manager.getDealership();

        assertEquals("D & B Used Cars", dealership.getName());
        assertEquals("111 Old Benbrook Rd", dealership.getAddress());
        assertEquals("817-555-5555", dealership.getPhone());

        List<Vehicle> vehicles = dealership.getAllVehicles();
        assertEquals(2, vehicles.size());

        Vehicle first = vehicles.get(0);
        assertEquals(10112, first.getVin());
        assertEquals(1993, first.getYear());
        assertEquals("Ford", first.getMake());
        assertEquals("Explorer", first.getModel());
        assertEquals("SUV", first.getVehicleType());
        assertEquals("Red", first.getColor());
        assertEquals(525123, first.getOdometer());
        assertEquals(new BigDecimal("995.00"), first.getPrice());
    }

    @Test
    void skipsInvalidAndBlankLines() throws IOException {
        DealershipFileManager manager = managerFor(
                "D & B Used Cars|111 Old Benbrook Rd|817-555-5555",
                "10112|1993|Ford|Explorer|SUV|Red|525123|995.00",
                "",
                "not|enough|fields",
                "abc|2001|Ford|Ranger|truck|Yellow|172544|1995.00",
                "44901|2012|Honda|Civic|car|Gray|103221|6995.00");

        assertEquals(2, manager.getDealership().getAllVehicles().size());
    }

    @Test
    void throwsWhenFileDoesNotExist() {
        DealershipFileManager manager = new DealershipFileManager(tempDir.resolve("missing.csv").toString());
        assertThrows(IllegalStateException.class, manager::getDealership);
    }

    @Test
    void saveThenLoadGivesTheSameInventory() throws IOException {
        DealershipFileManager manager = managerFor(
                "D & B Used Cars|111 Old Benbrook Rd|817-555-5555",
                "10112|1993|Ford|Explorer|SUV|Red|525123|995.00");
        Dealership dealership = manager.getDealership();
        dealership.addVehicle(new Vehicle(44901, 2012, "Honda", "Civic", "car", "Gray", 103221, new BigDecimal("6995")));

        manager.saveDealership(dealership);
        Dealership reloaded = manager.getDealership();

        assertEquals("D & B Used Cars", reloaded.getName());
        assertEquals(2, reloaded.getAllVehicles().size());
        Vehicle civic = reloaded.getVehicleByVin(44901);
        assertEquals("Civic", civic.getModel());
        assertEquals(new BigDecimal("6995.00"), civic.getPrice());

        // Prices are always written with two decimals
        List<String> lines = Files.readAllLines(tempDir.resolve("inventory.csv"));
        assertEquals("44901|2012|Honda|Civic|car|Gray|103221|6995.00", lines.get(2));
    }

    @Test
    void loadsTheSampleInventoryFile() {
        // Maven runs tests from the project root, so the default path resolves to data/inventory.csv
        Dealership dealership = new DealershipFileManager().getDealership();
        assertEquals("D & B Used Cars", dealership.getName());
        assertFalse(dealership.getAllVehicles().isEmpty());
    }
}
