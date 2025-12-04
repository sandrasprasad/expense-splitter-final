package com.app.expense_splitter.service;

import com.app.expense_splitter.exception.UserAlreadyExistsException;
import com.app.expense_splitter.model.dto.UserLoginRequest;
import com.app.expense_splitter.model.dto.UserLoginResponse;
import com.app.expense_splitter.model.dto.UserRegisterRequest;
import com.app.expense_splitter.model.dto.UserRegisterResponse;
import com.app.expense_splitter.model.entity.User;
import com.app.expense_splitter.repository.UserRepository;
import com.app.expense_splitter.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Generates constructor for final fields
@Slf4j // Use this for logging
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId()); // Logging

        // Map to Response DTO
        UserRegisterResponse response = new UserRegisterResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setPhoneNumber(savedUser.getPhoneNumber());
        return response;
    }

    @Override
    public UserLoginResponse login(UserLoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        UserLoginResponse loginResponse = new UserLoginResponse();
        loginResponse.setMessage("Login Successfully");
        loginResponse.setUserId(user.getId());
        loginResponse.setName(user.getName());
        loginResponse.setToken(token);
        return loginResponse;
    }
}