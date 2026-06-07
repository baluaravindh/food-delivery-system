package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantResponseDTO {

    private Long id;
    private String restaurantName;
    private String description;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String cuisineType;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String imageUrl;
    private boolean isActive;
    private boolean isApproved;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
}
