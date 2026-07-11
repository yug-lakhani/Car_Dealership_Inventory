package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.exception.VehicleModelAlreadyExistsException;
import com.dealership.inventory.security.JwtAuthFilter;
import com.dealership.inventory.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link VehicleController}. The security filter chain is
 * disabled here so the focus stays on request mapping, validation, and
 * exception handling; admin-only access is covered separately by the service
 * security test.
 */
@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void createVehicle_returns201Created_withVehicleDetails_whenRequestIsValid() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);
        VehicleResponse response = new VehicleResponse(
                1L, "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);

        when(vehicleService.createVehicle(any(CreateVehicleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.category").value("Sedan"))
                .andExpect(jsonPath("$.price").value(24999.99))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void createVehicle_returns400BadRequest_whenMakeIsBlank() throws Exception {
        String invalidPayload = """
                {"make":"","model":"Camry","category":"Sedan","price":24999.99,"quantity":3}
                """;

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(vehicleService);
    }

    @Test
    void createVehicle_returns409Conflict_whenModelAlreadyExists() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3);
        when(vehicleService.createVehicle(any(CreateVehicleRequest.class)))
                .thenThrow(new VehicleModelAlreadyExistsException("Camry"));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}