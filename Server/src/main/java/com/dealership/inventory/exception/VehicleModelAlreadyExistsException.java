package com.dealership.inventory.exception;

public class VehicleModelAlreadyExistsException extends RuntimeException {

    public VehicleModelAlreadyExistsException(String model) {
        super("Vehicle model already exists: " + model);
    }
}