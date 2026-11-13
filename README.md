# Car Dealership

A console-based inventory manager for a car dealership, written in Java to practise object-oriented programming.

A sales manager can search the inventory, list all vehicles, and add or remove vehicles. The inventory is stored in a pipe-delimited CSV file and saved automatically after every change.

```text
===== CAR DEALERSHIP =====

1. Find vehicles by price range
2. Find vehicles by make/model
3. Find vehicles by year range
4. Find vehicles by color
5. Find vehicles by mileage range
6. Find vehicles by type
7. List all vehicles
8. Add a vehicle
9. Remove a vehicle

99. Quit

Enter your choice:
```

## Requirements

- Java 17 or newer (`java -version`)
- Maven 3.8 or newer (`mvn -v`), or an IDE with Maven support such as IntelliJ IDEA

No other software is needed. The application has no runtime dependencies; JUnit 5 is used for tests only.

## Build and run

Run all commands from the project root (the folder that contains `pom.xml`). The app reads `data/inventory.csv` relative to the folder it is started from.

```bash
# Compile, run the tests and build the jar
mvn package

# Start the application from the jar
java -jar target/car-dealership-1.0-SNAPSHOT.jar

# Or start it directly with Maven
mvn compile exec:java
```

In IntelliJ IDEA: open the folder as a Maven project and run `com.dealership.Program`. Make sure the run configuration's working directory is the project root.

## Run the tests

```bash
mvn test
```

The tests cover adding and removing vehicles, duplicate and invalid data, every search method, and loading and saving the CSV file. The file tests use a temporary file, so `data/inventory.csv` is never changed by the tests.

## Using the application

| Option | What it asks for | Notes |
|---|---|---|
| 1. Price range | minimum and maximum price | e.g. `1000` or `1995.00` (use a dot, not a comma) |
| 2. Make/model | make, then model | press Enter at model to see all models of that make |
| 3. Year range | minimum and maximum year | |
| 4. Color | color | |
| 5. Mileage range | minimum and maximum mileage | |
| 6. Type | vehicle type | e.g. `car`, `truck`, `SUV`, `van` |
| 7. List all | nothing | |
| 8. Add | VIN, year, make, model, type, color, odometer, price | saved automatically |
| 9. Remove | VIN, then confirmation (`y`/`n`) | saved automatically |

- Text searches ignore upper/lower case (`ford` finds `Ford`).
- Ranges include both ends (year 2001 to 2012 includes 2001 and 2012).
- Invalid input never crashes the app: you are asked again, or told why a vehicle was not added.

### Validation rules for a new vehicle

- The VIN must be a positive number and must not already be in the inventory.
- The year must be between 1886 and next year.
- Make, model, type and color cannot be empty or contain `|`.
- The odometer cannot be negative.
- The price must be greater than 0.

## Inventory file format

`data/inventory.csv` uses `|` as the separator. The first line holds the dealership's details; every other line is one vehicle.

```text
name|address|phone
vin|year|make|model|type|color|odometer|price
```

```text
D & B Used Cars|111 Old Benbrook Rd|817-555-5555
10112|1993|Ford|Explorer|SUV|Red|525123|995.00
37846|2001|Ford|Ranger|truck|Yellow|172544|1995.00
```

If a vehicle line is broken (wrong number of fields, text where a number should be, or a duplicate VIN), it is skipped with a warning and the rest of the inventory still loads.

## Project structure

```text
├── pom.xml
├── data/inventory.csv
└── src
    ├── main/java/com/dealership
    │   ├── Program.java                         entry point
    │   ├── models/Vehicle.java                  one vehicle (data only)
    │   ├── services/Dealership.java             inventory, searches, business rules
    │   ├── persistence/DealershipFileManager.java   reads and writes the CSV file
    │   └── ui/UserInterface.java                menu, input and output
    └── test/java/com/dealership
        ├── services/DealershipTest.java
        └── persistence/DealershipFileManagerTest.java
```

## Design

Each class has one job, and each one only uses the classes below it:

```text
Program ─▶ UserInterface ─▶ Dealership ─▶ Vehicle
                 │
                 └────────▶ DealershipFileManager
```

- **`Vehicle`** only holds data.
- **`Dealership`** owns the inventory list. All searching, adding, removing and validation happens here. `getAllVehicles()` returns a copy, so no other class can change the list behind its back.
- **`DealershipFileManager`** is the only class that knows about the file format.
- **`UserInterface`** is the only class that uses `System.in` and `System.out`. It reads input, asks `Dealership` for results and prints them. It never loops over the inventory itself.
- **`Program`** just starts the user interface.

Some smaller decisions:

- Prices use `BigDecimal` instead of `double`, so amounts like `0.10 + 0.20` are exact.
- All six search methods share one private `filter()` helper, so the search loop is written only once.
- All keyboard input is read with `scanner.nextLine()`. This avoids the common bug where `nextInt()` leaves the Enter key in the buffer.
