package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.exception.EmailAlreadyExistsException;
import com.dealership.inventory.repository.UserRepository;
import com.dealership.inventory.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // ModelMapper copies username/email/password 1:1 by field name;
        // password is immediately overwritten with its hashed form below,
        // and role/id/createdAt are deliberately absent from the request
        // (they aren't client-supplied), so they're set/generated separately.
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        // ModelMapper can't construct RegisterResponse: it's a record, and
        // records have no setters - only a canonical constructor. Mapped
        // manually instead; see RegisterResponse.from for details.
        return RegisterResponse.from(savedUser);
    }
}
