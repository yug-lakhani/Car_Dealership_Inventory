package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.request.UpdateVehicleRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void getVehicles_returns200Ok_withPagedVehicleList() throws Exception {
        Page<VehicleResponse> page = new PageImpl<>(
                List.of(new VehicleResponse(1L, "Toyota", "Camry", "Sedan", new BigDecimal("24999.99"), 3)),
                PageRequest.of(0, 10, Sort.by("make").ascending()),
                1);

        when(vehicleService.getVehicles(any())).thenReturn(page);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].make").value("Toyota"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchVehicles_returns200Ok_withFilteredVehicleList() throws Exception {
        Page<VehicleResponse> page = new PageImpl<>(
                List.of(new VehicleResponse(1L, "BMW", "X5", "SUV", new BigDecimal("65000.00"), 2)),
                PageRequest.of(0, 10),
                1);

        when(vehicleService.searchVehicles(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/vehicles/search?make=BMW&category=SUV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].model").value("X5"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateVehicle_returns200Ok_withUpdatedVehicle() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);
        VehicleResponse response = new VehicleResponse(
                1L, "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);

        when(vehicleService.updateVehicle(any(), any(UpdateVehicleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.model").value("Highlander"))
                .andExpect(jsonPath("$.quantity").value(6));
    }

    @Test
    void deleteVehicle_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());
    }
}