package com.dealership.inventory.dto.response;

import com.dealership.inventory.entity.Vehicle;

import java.math.BigDecimal;

/**
 * Vehicle details returned to API clients.
 */
public record VehicleResponse(
        Long id,
        String make,
        String model,
        String category,
        BigDecimal price,
        Integer quantity
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getCategory(),
                vehicle.getPrice(),
                vehicle.getQuantity());
    }
}