package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.CreateVehicleRequest;
import com.dealership.inventory.dto.request.RestockVehicleRequest;
import com.dealership.inventory.dto.request.UpdateVehicleRequest;
import com.dealership.inventory.dto.response.VehicleResponse;
import com.dealership.inventory.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Vehicle endpoints. Kept thin: validation and response mapping only.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getVehicles(Pageable pageable) {
        return ResponseEntity.ok(vehicleService.getVehicles(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VehicleResponse>> searchVehicles(@RequestParam(required = false) String make,
                                                                @RequestParam(required = false) String model,
                                                                @RequestParam(required = false) String category,
                                                                @RequestParam(required = false) BigDecimal minPrice,
                                                                @RequestParam(required = false) BigDecimal maxPrice,
                                                                Pageable pageable) {
        return ResponseEntity.ok(vehicleService.searchVehicles(make, model, category, minPrice, maxPrice, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<VehicleResponse> purchaseVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.purchaseVehicle(id));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<VehicleResponse> restockVehicle(@PathVariable Long id,
                                                          @Valid @RequestBody RestockVehicleRequest request) {
        return ResponseEntity.ok(vehicleService.restockVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}