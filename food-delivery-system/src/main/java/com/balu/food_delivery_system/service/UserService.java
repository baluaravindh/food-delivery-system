package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.*;
import com.balu.food_delivery_system.entity.RefreshToken;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.exception.DuplicateUserFoundException;
import com.balu.food_delivery_system.exception.InvalidCredentialsException;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.UserRepository;
import com.balu.food_delivery_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {

        log.info("Registration attempt for email: {}", dto.getEmail());

        // Validate email not duplicate
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserFoundException("email already registered" + dto.getEmail());
        }

        // Build user entity using Java 17 builder
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);

        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getFullName());

        log.info("User registered successfully: {}", savedUser.getEmail());

        return mapToDto(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {

        log.info("Login attempt for email: {}", dto.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Validate password matches
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Validate account is active
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is deactivated. " + "Contact admin.");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        log.info("Login successful for: {}", user.getEmail());

        return LoginResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer ")
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public String changePassword(ChangePasswordRequestDTO dto) {

        // Get current logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("Password change request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Delete refresh token — force re-login
        refreshTokenService.deleteByUser(user);

        log.info("Password changed successfully: {}", email);

        return "Password changed successfully." + " Please login again.";
    }

    @Transactional
    public UserResponseDTO updateUserStatus(Long userId, boolean status) {

        log.info("Updating status for userId: {}" + " to: {}", userId, status);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + userId));

        // Update status
        user.setActive(status);
        User updatedUser = userRepository.save(user);

        log.info("User status updated: {} → {}", user.getEmail(), status);

        return mapToDto(updatedUser);
    }

    public List<UserResponseDTO> getAllUsers() {

        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    //---MAPPER---
    private UserResponseDTO mapToDto(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.isActive())
                .isEmailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
