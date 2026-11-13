package com.dealership;

import com.dealership.ui.UserInterface;

/**
 * Entry point of the Car Dealership application.
 *
 * <p>{@code main} is deliberately tiny: it only creates the user interface and
 * starts it. All real work happens in the other classes:
 * <pre>
 * Program  →  UserInterface  →  Dealership  →  Vehicle
 *                   └──────────→  DealershipFileManager
 * </pre>
 */
public class Program {

    /**
     * Starts the application. Java calls this method when the program is run.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        ui.display(); // returns when the user chooses 99 (Quit)
    }
}
