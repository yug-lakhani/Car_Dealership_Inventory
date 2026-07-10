package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.exception.EmailAlreadyExistsException;
import com.dealership.inventory.repository.UserRepository;
import com.dealership.inventory.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    /**
     * Enforced here (service layer) rather than only on the controller, so
     * the admin-only rule holds even if this method is ever called from
     * somewhere other than {@code AdminController}.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public RegisterResponse createAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN);

        User savedUser = userRepository.save(user);

        return RegisterResponse.from(savedUser);
    }
}
