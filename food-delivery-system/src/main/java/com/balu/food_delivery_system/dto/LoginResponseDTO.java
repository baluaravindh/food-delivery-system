package com.balu.food_delivery_system.dto;

import com.balu.food_delivery_system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private User.Role role;
    private String token;
    private String tokenType = "Bearer";
    private String refreshToken;
}
