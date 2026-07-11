package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.entity.Vehicle;
import com.dealership.inventory.exception.VehicleModelAlreadyExistsException;
import com.dealership.inventory.mapper.VehicleMapper;
import com.dealership.inventory.repository.VehicleRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VehicleServiceImpl}. Method security is verified
 * separately by {@code VehicleServiceSecurityTest} because {@code @PreAuthorize}
 * is enforced by Spring's proxy, not by a directly constructed bean.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    private Validator validator;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        vehicleService = new VehicleServiceImpl(vehicleRepository, vehicleMapper, validator);
    }

    @Test
    void createVehicle_savesVehicleAndReturnsCreatedDetails() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);
        Vehicle mappedVehicle = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(new BigDecimal("24999.99"))
                .quantity(3)
                .build();
        Vehicle savedVehicle = Vehicle.builder()
                .id(11L)
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(new BigDecimal("24999.99"))
                .quantity(3)
                .build();

        when(vehicleMapper.toEntity(request)).thenReturn(mappedVehicle);
        when(vehicleRepository.save(mappedVehicle)).thenReturn(savedVehicle);
        when(vehicleMapper.toResponse(savedVehicle)).thenReturn(VehicleResponse.from(savedVehicle));

        VehicleResponse response = vehicleService.createVehicle(request);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.make()).isEqualTo("Toyota");
        assertThat(response.model()).isEqualTo("Camry");
        assertThat(response.category()).isEqualTo("Sedan");
        assertThat(response.price()).isEqualByComparingTo("24999.99");
        assertThat(response.quantity()).isEqualTo(3);

        ArgumentCaptor<Vehicle> savedVehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(savedVehicleCaptor.capture());
        assertThat(savedVehicleCaptor.getValue()).isSameAs(mappedVehicle);
    }

    @Test
    void createVehicle_throwsConstraintViolationException_whenRequestIsInvalid() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "", "", "", new BigDecimal("0.00"), -1);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("make")
                .hasMessageContaining("price")
                .hasMessageContaining("quantity");

        verifyNoInteractions(vehicleRepository, vehicleMapper);
    }

    @Test
    void createVehicle_propagatesRepositoryFailures() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);
        Vehicle mappedVehicle = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(new BigDecimal("24999.99"))
                .quantity(3)
                .build();

        when(vehicleMapper.toEntity(request)).thenReturn(mappedVehicle);
        when(vehicleRepository.save(mappedVehicle)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(vehicleMapper, never()).toResponse(any());
    }

    @Test
    void createVehicle_throwsConflictWhenModelAlreadyExists() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);

        when(vehicleRepository.existsByModel("Camry")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(VehicleModelAlreadyExistsException.class)
                .hasMessageContaining("Camry");

        verify(vehicleRepository).existsByModel("Camry");
        verifyNoInteractions(vehicleMapper);
    }
}