package com.dealership.inventory.service;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.request.RestockVehicleRequest;
import com.dealership.inventory.dto.request.UpdateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);

    Page<VehicleResponse> getVehicles(Pageable pageable);

    Page<VehicleResponse> searchVehicles(String make,
                                         String model,
                                         String category,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         Pageable pageable);

    VehicleResponse updateVehicle(@Positive Long id, UpdateVehicleRequest request);

    void deleteVehicle(@Positive Long id);

    VehicleResponse purchaseVehicle(@Positive Long id);

    VehicleResponse restockVehicle(@Positive Long id, RestockVehicleRequest request);
}