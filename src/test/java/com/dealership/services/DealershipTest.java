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

    /** Fills the dealership with the same vehicles as the sample inventory file. */
    private void addSampleInventory() {
        dealership.addVehicle(new Vehicle(10112, 1993, "Ford", "Explorer", "SUV", "Red", 525123, new BigDecimal("995.00")));
        dealership.addVehicle(new Vehicle(37846, 2001, "Ford", "Ranger", "truck", "Yellow", 172544, new BigDecimal("1995.00")));
        dealership.addVehicle(new Vehicle(44901, 2012, "Honda", "Civic", "car", "Gray", 103221, new BigDecimal("6995.00")));
    }

    /** Returns just the VINs, which makes assertions short and readable. */
    private List<Integer> vins(List<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getVin).toList();
    }

    // ---------------- addVehicle / getAllVehicles ----------------

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
    void addVehicleRejectsInvalidData() {
        Vehicle badPrice = new Vehicle(1, 2012, "Honda", "Civic", "car", "Gray", 1000, BigDecimal.ZERO);
        Vehicle badYear = new Vehicle(2, 1500, "Honda", "Civic", "car", "Gray", 1000, BigDecimal.TEN);
        Vehicle negativeOdometer = new Vehicle(3, 2012, "Honda", "Civic", "car", "Gray", -5, BigDecimal.TEN);
        Vehicle emptyMake = new Vehicle(4, 2012, " ", "Civic", "car", "Gray", 1000, BigDecimal.TEN);
        Vehicle pipeInModel = new Vehicle(5, 2012, "Honda", "Civ|ic", "car", "Gray", 1000, BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class, () -> dealership.addVehicle(badPrice));
        assertThrows(IllegalArgumentException.class, () -> dealership.addVehicle(badYear));
        assertThrows(IllegalArgumentException.class, () -> dealership.addVehicle(negativeOdometer));
        assertThrows(IllegalArgumentException.class, () -> dealership.addVehicle(emptyMake));
        assertThrows(IllegalArgumentException.class, () -> dealership.addVehicle(pipeInModel));
        assertTrue(dealership.getAllVehicles().isEmpty());
    }

    @Test
    void getAllVehiclesReturnsCopyThatCannotChangeInventory() {
        dealership.addVehicle(sampleVehicle(44901));
        List<Vehicle> copy = dealership.getAllVehicles();
        copy.clear();
        assertEquals(1, dealership.getAllVehicles().size());
    }

    // ---------------- removeVehicle ----------------

    @Test
    void removeVehicleRemovesByVin() {
        addSampleInventory();
        Vehicle ranger = dealership.getVehicleByVin(37846);

        assertTrue(dealership.removeVehicle(ranger));
        assertNull(dealership.getVehicleByVin(37846));
        assertEquals(2, dealership.getAllVehicles().size());
    }

    @Test
    void removeVehicleReturnsFalseWhenNotInStock() {
        addSampleInventory();
        assertFalse(dealership.removeVehicle(sampleVehicle(99999)));
        assertFalse(dealership.removeVehicle(null));
        assertEquals(3, dealership.getAllVehicles().size());
    }

    // ---------------- searches ----------------

    @Test
    void getVehiclesByPriceIncludesBothEnds() {
        addSampleInventory();
        List<Vehicle> result = dealership.getVehiclesByPrice(new BigDecimal("995"), new BigDecimal("1995.00"));
        assertEquals(List.of(10112, 37846), vins(result));
    }

    @Test
    void getVehiclesByMakeModelIsCaseInsensitive() {
        addSampleInventory();
        assertEquals(List.of(37846), vins(dealership.getVehiclesByMakeModel("FORD", "ranger")));
    }

    @Test
    void getVehiclesByMakeModelWithEmptyModelReturnsAllOfThatMake() {
        addSampleInventory();
        assertEquals(List.of(10112, 37846), vins(dealership.getVehiclesByMakeModel("ford", "")));
    }

    @Test
    void getVehiclesByYear() {
        addSampleInventory();
        assertEquals(List.of(37846, 44901), vins(dealership.getVehiclesByYear(2000, 2012)));
    }

    @Test
    void getVehiclesByColorIsCaseInsensitive() {
        addSampleInventory();
        assertEquals(List.of(10112), vins(dealership.getVehiclesByColor("rED")));
    }

    @Test
    void getVehiclesByMileage() {
        addSampleInventory();
        assertEquals(List.of(37846, 44901), vins(dealership.getVehiclesByMileage(100000, 200000)));
    }

    @Test
    void getVehiclesByTypeIsCaseInsensitive() {
        addSampleInventory();
        assertEquals(List.of(10112), vins(dealership.getVehiclesByType("suv")));
    }

    @Test
    void searchWithNoMatchesReturnsEmptyList() {
        addSampleInventory();
        assertTrue(dealership.getVehiclesByColor("Purple").isEmpty());
    }
}
