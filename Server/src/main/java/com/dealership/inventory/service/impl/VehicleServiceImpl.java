package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.request.UpdateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.entity.Vehicle;
import com.dealership.inventory.exception.VehicleModelAlreadyExistsException;
import com.dealership.inventory.exception.VehicleNotFoundException;
import com.dealership.inventory.mapper.VehicleMapper;
import com.dealership.inventory.repository.VehicleRepository;
import com.dealership.inventory.repository.specification.VehicleSpecifications;
import com.dealership.inventory.service.VehicleService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final Validator validator;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        validateRequest(request);
        ensureModelDoesNotAlreadyExist(request.model());

        Vehicle vehicle = vehicleMapper.toEntity(request);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<VehicleResponse> getVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable)
                .map(vehicleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<VehicleResponse> searchVehicles(String make,
                                                String model,
                                                String category,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Pageable pageable) {
        Specification<Vehicle> specification = Specification.where(VehicleSpecifications.hasMake(make))
                .and(VehicleSpecifications.hasModel(model))
                .and(VehicleSpecifications.hasCategory(category))
                .and(VehicleSpecifications.priceGreaterThanOrEqualTo(minPrice))
                .and(VehicleSpecifications.priceLessThanOrEqualTo(maxPrice));

        return vehicleRepository.findAll(specification, pageable)
                .map(vehicleMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        validateRequest(request);

        Vehicle vehicle = findVehicleById(id);
        ensureModelDoesNotAlreadyExistForUpdate(request.model(), id);
        applyUpdate(vehicle, request);

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    private void validateRequest(CreateVehicleRequest request) {
        Set<ConstraintViolation<CreateVehicleRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void validateRequest(UpdateVehicleRequest request) {
        Set<ConstraintViolation<UpdateVehicleRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void ensureModelDoesNotAlreadyExist(String model) {
        if (vehicleRepository.existsByModel(model)) {
            throw new VehicleModelAlreadyExistsException(model);
        }
    }

    private void ensureModelDoesNotAlreadyExistForUpdate(String model, Long id) {
        if (vehicleRepository.existsByModelAndIdNot(model, id)) {
            throw new VehicleModelAlreadyExistsException(model);
        }
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private void applyUpdate(Vehicle vehicle, UpdateVehicleRequest request) {
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setCategory(request.category());
        vehicle.setPrice(request.price());
        vehicle.setQuantity(request.quantity());
    }
}