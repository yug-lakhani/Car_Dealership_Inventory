package com.dealership.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Payload for creating a vehicle.
 */
public record CreateVehicleRequest(

        @NotBlank(message = "Make is required")
        String make,

        @NotBlank(message = "Model is required")
        String model,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be greater than or equal to 0")
        Integer quantity
) {
}