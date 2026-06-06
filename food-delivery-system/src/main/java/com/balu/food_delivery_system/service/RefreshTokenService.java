package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.entity.RefreshToken;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.exception.InvalidCredentialsException;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.RefreshTokenRepository;
import com.balu.food_delivery_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // WHO: system after login
    // WHAT: create or replace refresh token for user
    // WHAT to return: RefreshToken entity
    @Transactional
    public RefreshToken createRefreshToken(String email) {

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email : " + email));

        // Delete existing token if present
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        // Create new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // WHO: AuthController on refresh request
    // WHAT: validate refresh token not expired
    // WHAT to return: RefreshToken if valid
    public RefreshToken validateRefreshToken(String token) {

        // Find token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Refresh token not found with refresh token : " + token));

        // Check if token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired. Please login again.");
        }

        return refreshToken;
    }

    // WHO: AuthController on logout
    // WHAT: delete refresh token for user
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
