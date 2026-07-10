package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative endpoints. Kept thin: request/response mapping and
 * validation only - the admin-only restriction and business logic live in
 * {@link AdminService}.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register-admin")
    public ResponseEntity<RegisterResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
