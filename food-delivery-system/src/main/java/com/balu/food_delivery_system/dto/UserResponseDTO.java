package com.balu.food_delivery_system.dto;

import com.balu.food_delivery_system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private User.Role role;
    private boolean isActive;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
}
