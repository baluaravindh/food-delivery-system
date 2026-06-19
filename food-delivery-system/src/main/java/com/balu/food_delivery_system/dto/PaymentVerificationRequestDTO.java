package com.balu.food_delivery_system.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequestDTO {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private Long orderId;
}
