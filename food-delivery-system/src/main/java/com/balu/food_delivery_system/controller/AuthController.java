package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.*;
import com.balu.food_delivery_system.entity.RefreshToken;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.security.JwtUtil;
import com.balu.food_delivery_system.service.RefreshTokenService;
import com.balu.food_delivery_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, Login, Logout APIs")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(dto));
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @Operation(summary = "Refresh JWT token")
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO dto) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(dto.getRefreshToken());

        String newToken = jwtUtil.generateToken(refreshToken.getUser().getEmail());

        return ResponseEntity.ok(LoginResponseDTO.builder().token(newToken).refreshToken(dto.getRefreshToken()).build());
    }

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = new User();
        user.setEmail(email);

        log.info("Logout for: {}", email);

        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        return ResponseEntity.ok(userService.changePassword(dto));
    }
}
