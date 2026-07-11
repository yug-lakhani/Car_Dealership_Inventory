package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.entity.Vehicle;
import com.dealership.inventory.mapper.VehicleMapper;
import com.dealership.inventory.repository.VehicleRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Security-focused service test that proves {@code @PreAuthorize("hasRole('ADMIN')")}
 * is enforced by Spring's proxy.
 */
@SpringBootTest
@ActiveProfiles("test")
class VehicleServiceSecurityTest {

    @Autowired
    private VehicleServiceImpl vehicleService;

    @MockBean
    private VehicleRepository vehicleRepository;

    @MockBean
    private VehicleMapper vehicleMapper;

    @MockBean
    private Validator validator;

    @Test
    @WithMockUser(roles = "USER")
    void createVehicle_returnsAccessDeniedForNonAdminUsers() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);
        when(validator.validate(any(CreateVehicleRequest.class))).thenReturn(Collections.emptySet());

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(vehicleRepository, vehicleMapper);
    }
}