package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.OrderResponseDTO;
import com.balu.food_delivery_system.dto.PaymentOrderResponseDTO;
import com.balu.food_delivery_system.dto.PaymentVerificationRequestDTO;
import com.balu.food_delivery_system.service.PaymentService;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/create-order/{orderId}
    // Access: CUSTOMER only
    // Request: orderId as PathVariable
    // Response: 200 + PaymentOrderResponseDTO
    // Note: throws RazorpayException
    @Operation(summary = "Create Payment Order")
    @PostMapping("create-order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentOrderResponseDTO> createPaymentOrder(
            @PathVariable Long orderId) throws RazorpayException {
        return ResponseEntity.ok().body(paymentService.createPaymentOrder(orderId));
    }

    // POST /api/payments/verify
    // Access: CUSTOMER only
    // Request: @RequestBody PaymentVerificationRequestDTO
    // Response: 200 + OrderResponseDTO
    // Note: throws RazorpayException
    @Operation(summary = "Verify Payment")
    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDTO> verifyPayment(
            @RequestBody PaymentVerificationRequestDTO dto) throws RazorpayException {
        return ResponseEntity.ok().body(paymentService.verifyPayment(dto));
    }
}
