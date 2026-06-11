package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemResponseDTO implements Serializable {

    // id
    private Long id;
    // itemName
    private String itemName;
    // description
    private String itemDescription;
    // price
    private BigDecimal price;
    // isVegetarian
    private boolean isVegetarian;
    // isAvailable
    private boolean isAvailable;
    // imageUrl
    private String imageUrl;
    // categoryId
    private Long categoryId;
    // categoryName
    private String categoryName;
    // restaurantId
    private Long restaurantId;
    // restaurantName
    private String restaurantName;
    // createdAt
    private LocalDateTime createdAt;
}
