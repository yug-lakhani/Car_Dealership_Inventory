package com.dealership.inventory.mapper;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.entity.Vehicle;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for vehicle request/response conversion.
 */
@Component
public class VehicleMapper {

    public Vehicle toEntity(CreateVehicleRequest request) {
        return Vehicle.builder()
                .make(request.make())
                .model(request.model())
                .category(request.category())
                .price(request.price())
                .quantity(request.quantity())
                .build();
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.from(vehicle);
    }
}