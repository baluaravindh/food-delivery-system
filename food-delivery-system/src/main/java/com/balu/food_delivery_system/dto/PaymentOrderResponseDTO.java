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
public class PaymentOrderResponseDTO {

    private String razorpayOrderId;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
}
