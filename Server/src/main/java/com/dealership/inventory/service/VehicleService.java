package com.dealership.inventory.service;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);
}