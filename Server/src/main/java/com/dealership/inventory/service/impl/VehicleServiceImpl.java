package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.entity.Vehicle;
import com.dealership.inventory.exception.VehicleModelAlreadyExistsException;
import com.dealership.inventory.mapper.VehicleMapper;
import com.dealership.inventory.repository.VehicleRepository;
import com.dealership.inventory.service.VehicleService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final Validator validator;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        validateRequest(request);
        ensureModelDoesNotAlreadyExist(request.model());

        Vehicle vehicle = vehicleMapper.toEntity(request);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(savedVehicle);
    }

    private void validateRequest(CreateVehicleRequest request) {
        Set<ConstraintViolation<CreateVehicleRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void ensureModelDoesNotAlreadyExist(String model) {
        if (vehicleRepository.existsByModel(model)) {
            throw new VehicleModelAlreadyExistsException(model);
        }
    }
}