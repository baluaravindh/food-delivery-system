package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponseDTO {

    // cartId
    private Long cartId;
    // customerId
    private Long userId;
    // customerName
    private String userFullName;
    // items (List<CartItemResponseDTO>)
    private List<CartItemResponseDTO> items;
    // totalItems (int — count of unique items)
    private int totalItems;
    // totalAmount (BigDecimal — sum of all prices)
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

}
