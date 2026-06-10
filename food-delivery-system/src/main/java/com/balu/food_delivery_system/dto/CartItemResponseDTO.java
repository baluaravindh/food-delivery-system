package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDTO {

    // cartItemId
    private Long cartItemId;
    // menuItemId
    private Long menuItemId;
    // itemName
    private String menuItemName;
    // itemDescription
    private String menuItemDescription;
    // price (per item)
    private BigDecimal price;
    // quantity
    private Integer quantity;
    // subTotal (price × quantity)
    private BigDecimal subTotal;
    // restaurantId
    private Long restaurantId;
    // restaurantName
    private String restaurantName;
    // isVegetarian
    private boolean isVegetarian;
}
