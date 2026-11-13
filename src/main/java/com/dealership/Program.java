package com.dealership;

import com.dealership.ui.UserInterface;

/**
 * Entry point of the Car Dealership application.
 * All it does is start the user interface.
 */
public class Program {

    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        ui.display();
    }
}
