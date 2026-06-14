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
public class OrderResponseDTO {

    // orderId
    private Long id;
    // customerId
    private Long userId;
    // customerName
    private String userFullName;
    // restaurantId
    private Long restaurantId;
    // restaurantName
    private String restaurantName;
    // status
    private String status;
    // paymentStatus
    private String paymentStatus;
    // totalAmount
    private BigDecimal totalAmount;
    // deliveryAddress
    private String deliveryAddress;
    // specialInstructions
    private String specialInstructions;
    // items (List<OrderItemResponseDTO>)
    private List<OrderItemResponseDTO> items;
    // createdAt
    private LocalDateTime createdAt;
}
