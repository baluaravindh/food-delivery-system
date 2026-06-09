package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuCategoryResponseDTO {

    private Long id;
    private String categoryName;
    private String categoryDescription;
    private boolean isActive;
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime createdAt;
}
