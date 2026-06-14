package com.balu.food_delivery_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationDTO implements Serializable {

    // orderId
    private Long orderId;
    // customerName
    private String userFullName;
    // restaurantName
    private String restaurantName;
    // restaurantId
    private Long restaurantId;
    // totalAmount
    private BigDecimal totalAmount;
    // deliveryAddress
    private String deliveryAddress;
    // itemCount
    private Integer itemCount;
    // status
    private String status;
}
