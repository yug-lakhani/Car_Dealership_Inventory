package com.dealership.inventory.exception;

public class VehicleOutOfStockException extends RuntimeException {

    public VehicleOutOfStockException(Long id) {
        super("Vehicle with id " + id + " is out of stock");
    }
}
