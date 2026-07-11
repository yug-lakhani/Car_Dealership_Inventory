package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.UpdateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.entity.Vehicle;
import com.dealership.inventory.exception.VehicleNotFoundException;
import com.dealership.inventory.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real JPA-backed tests for vehicle listing, search, update, delete, and
 * method-security boundaries on the service layer.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VehicleServiceCrudSearchTest {

    @Autowired
    private VehicleServiceImpl vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle camry;
    private Vehicle x5;
    private Vehicle rav4;
    private Vehicle escape;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        camry = vehicleRepository.save(createVehicle("Toyota", "Camry", "Sedan", "24999.99", 3));
        x5 = vehicleRepository.save(createVehicle("BMW", "X5", "SUV", "65000.00", 2));
        rav4 = vehicleRepository.save(createVehicle("Toyota", "RAV4", "SUV", "32000.00", 5));
        escape = vehicleRepository.save(createVehicle("Ford", "Escape", "SUV", "28000.00", 4));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getVehicles_returnsPaginatedAndSortedVehicles() {
        Page<VehicleResponse> page = vehicleService.getVehicles(PageRequest.of(0, 2, Sort.by("make").ascending()));

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent())
                .extracting(VehicleResponse::make)
                .containsExactly("BMW", "Ford");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_filtersByMake() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                "Toyota", null, null, null, null, PageRequest.of(0, 10, Sort.by("model").ascending()));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(VehicleResponse::model)
                .containsExactly("Camry", "RAV4");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_filtersByModel() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                null, "X5", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).model()).isEqualTo("X5");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_filtersByCategory() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                null, null, "SUV", null, null, PageRequest.of(0, 10, Sort.by("price").ascending()));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(VehicleResponse::model)
                .containsExactly("Escape", "RAV4", "X5");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_filtersByMinPrice() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                null, null, null, new BigDecimal("60000.00"), null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).model()).isEqualTo("X5");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_filtersByMaxPrice() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                null, null, null, null, new BigDecimal("30000.00"), PageRequest.of(0, 10, Sort.by("price").ascending()));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(VehicleResponse::model)
                .containsExactly("Camry", "Escape");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_combinesMultipleFilters() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                "Toyota", null, "SUV", new BigDecimal("30000.00"), null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).model()).isEqualTo("RAV4");
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchVehicles_returnsEmptyPage_whenNoResultsMatch() {
        Page<VehicleResponse> page = vehicleService.searchVehicles(
                "Tesla", null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_updatesRequestedVehicle() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);

        VehicleResponse response = vehicleService.updateVehicle(camry.getId(), request);

        assertThat(response.id()).isEqualTo(camry.getId());
        assertThat(response.model()).isEqualTo("Highlander");
        assertThat(response.price()).isEqualByComparingTo("45000.00");

        Vehicle persisted = vehicleRepository.findById(camry.getId()).orElseThrow();
        assertThat(persisted.getModel()).isEqualTo("Highlander");
        assertThat(persisted.getCategory()).isEqualTo("SUV");
        assertThat(persisted.getQuantity()).isEqualTo(6);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_throwsVehicleNotFoundException_whenVehicleIsMissing() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);

        assertThatThrownBy(() -> vehicleService.updateVehicle(9999L, request))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_throwsConstraintViolationException_whenVehicleIdIsInvalid() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);

        assertThatThrownBy(() -> vehicleService.updateVehicle(0L, request))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_throwsConstraintViolationException_whenRequestIsInvalid() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "", "", "", new BigDecimal("0.00"), -1);

        assertThatThrownBy(() -> vehicleService.updateVehicle(camry.getId(), request))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
                .hasMessageContaining("make")
                .hasMessageContaining("price")
                .hasMessageContaining("quantity");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_throwsConflict_whenModelAlreadyExists() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "X5", "SUV", new BigDecimal("45000.00"), 6);

        assertThatThrownBy(() -> vehicleService.updateVehicle(camry.getId(), request))
                .isInstanceOf(com.dealership.inventory.exception.VehicleModelAlreadyExistsException.class)
                .hasMessageContaining("X5");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVehicle_removesRequestedVehicle() {
        vehicleService.deleteVehicle(escape.getId());

        assertThat(vehicleRepository.findById(escape.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVehicle_throwsVehicleNotFoundException_whenVehicleIsMissing() {
        assertThatThrownBy(() -> vehicleService.deleteVehicle(9999L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    void getVehicles_throwsAuthenticationCredentialsNotFoundException_whenCallerIsUnauthenticated() {
        assertThatThrownBy(() -> vehicleService.getVehicles(PageRequest.of(0, 10)))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateVehicle_throwsAccessDeniedException_whenCallerIsNotAdmin() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(
                "Toyota", "Highlander", "SUV", new BigDecimal("45000.00"), 6);

        assertThatThrownBy(() -> vehicleService.updateVehicle(camry.getId(), request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteVehicle_throwsAccessDeniedException_whenCallerIsNotAdmin() {
        assertThatThrownBy(() -> vehicleService.deleteVehicle(camry.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Vehicle createVehicle(String make, String model, String category, String price, Integer quantity) {
        return Vehicle.builder()
                .make(make)
                .model(model)
                .category(category)
                .price(new BigDecimal(price))
                .quantity(quantity)
                .build();
    }
}