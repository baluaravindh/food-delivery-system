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
public class OrderItemResponseDTO {

    // orderItemId
    private Long id;
    // menuItemId
    private Long menuItemId;
    // itemName
    private String itemName;
    // quantity
    private Integer quantity;
    // priceAtOrder
    private BigDecimal price;
    // subTotal (price × quantity)
    private BigDecimal subtotal;
}
