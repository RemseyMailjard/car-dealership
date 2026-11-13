package com.dealership.services;

import com.dealership.models.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DealershipTest {

    private Dealership dealership;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("D & B Used Cars", "111 Old Benbrook Rd", "817-555-5555");
    }

    private Vehicle sampleVehicle(int vin) {
        return new Vehicle(vin, 2012, "Honda", "Civic", "car", "Gray", 103221, new BigDecimal("6995.00"));
    }

    @Test
    void newDealershipHasEmptyInventory() {
        assertTrue(dealership.getAllVehicles().isEmpty());
    }

    @Test
    void addVehicleAddsToInventory() {
        assertTrue(dealership.addVehicle(sampleVehicle(44901)));
        assertEquals(1, dealership.getAllVehicles().size());
    }

    @Test
    void addVehicleRejectsDuplicateVin() {
        dealership.addVehicle(sampleVehicle(44901));
        assertFalse(dealership.addVehicle(sampleVehicle(44901)));
        assertEquals(1, dealership.getAllVehicles().size());
    }

    @Test
    void addVehicleRejectsNull() {
        assertFalse(dealership.addVehicle(null));
    }

    @Test
    void getAllVehiclesReturnsCopyThatCannotChangeInventory() {
        dealership.addVehicle(sampleVehicle(44901));
        List<Vehicle> copy = dealership.getAllVehicles();
        copy.clear();
        assertEquals(1, dealership.getAllVehicles().size());
    }
}
