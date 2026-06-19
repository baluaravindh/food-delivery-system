package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.OrderItemResponseDTO;
import com.balu.food_delivery_system.dto.OrderResponseDTO;
import com.balu.food_delivery_system.dto.PaymentOrderResponseDTO;
import com.balu.food_delivery_system.dto.PaymentVerificationRequestDTO;
import com.balu.food_delivery_system.entity.Order;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    // fields: RazorpayClient, OrderRepository
    // @Value: razorpay.key.id, razorpay.key.secret
    @Value("${razorpay.key.id}")
    private String razorpayId;

    @Value("${razorpay.key.secret}")
    private String razorpayKey;

    private final OrderRepository orderRepository;
    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(razorpayId, razorpayKey);
    }

    // METHOD 1: createPaymentOrder
    // WHO: CUSTOMER only (their own order)
    // WHAT to validate:
    // WHAT to do:
    // WHAT to return: PaymentOrderResponseDTO
    public PaymentOrderResponseDTO createPaymentOrder(Long orderId) throws RazorpayException {

        // Logged Customer
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   - order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        //   - order belongs to logged in customer
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this order");
        }

        //   - paymentStatus is still PENDING (not already paid)
        if (order.getPaymentStatus() != Order.PaymentStatus.PENDING) {
            throw new RuntimeException("Order is already paid");
        }

        //   - build Razorpay order options (amount in paise, currency INR, receipt = orderId)
        JSONObject options = new JSONObject();
        options.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        //   - call razorpayClient.orders.create(options)
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

        //   - save razorpayOrderId to order entity
        order.setPaymentOrderId(razorpayOrder.get("id"));
        orderRepository.save(order);

        //   - return PaymentOrderResponseDTO
        return PaymentOrderResponseDTO.builder()
                .razorpayOrderId(order.getPaymentOrderId())
                .orderId(orderId)
                .amount(order.getTotalAmount())
                .currency("INR")
                .status(order.getPaymentStatus().name())
                .build();
    }

    // METHOD 2: verifyPayment
    // WHO: CUSTOMER (called after payment completed)
    //   - verify Razorpay signature using HMAC SHA256
    //     (razorpayOrderId + "|" + razorpayPaymentId, signed with key secret)
    //   - throw exception if signature doesn't match (tampered payment)
    // WHAT to validate:
    // WHAT to do:
    //   - find order by orderId
    //   - set paymentStatus = PAID
    //   - set status = CONFIRMED
    //   - save order
    // WHAT to return: OrderResponseDTO
    public OrderResponseDTO verifyPayment(PaymentVerificationRequestDTO dto) throws RazorpayException {

        //   Step 1: Build the signature payload
        //           String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId()
        String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

        //   Step 2: Verify signature using Razorpay Utils
        //           RazorpayUtils.verifyPaymentSignature(JSONObject, String)
        //           throw RuntimeException if signature doesn't match
        JSONObject signatureParams = new JSONObject();
        signatureParams.put("razorpay_order_id", dto.getRazorpayOrderId());
        signatureParams.put("razorpay_payment_id", dto.getRazorpayPaymentId());
        signatureParams.put("razorpay_signature", dto.getRazorpaySignature());
        boolean isValid = Utils.verifyPaymentSignature(signatureParams, razorpayKey);
        if (!isValid) {
            throw new RuntimeException("Payment verification failed. Invalid signature.");
        }

        //   Step 3: Find order by dto.getOrderId()
        //           orElseThrow ResourceNotFoundException
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.getOrderId()));

        //   Step 4: Set order.setPaymentStatus(PAID)
        order.setPaymentStatus(Order.PaymentStatus.PAID);

        //   Step 5: Set order.setStatus(CONFIRMED)
        order.setStatus(Order.OrderStatus.CONFIRMED);

        //   Step 6: Save order
        Order savedOrder = orderRepository.save(order);

        //   Step 7: Return mapToDto(savedOrder)
        //           (copy the mapToDto method from OrderService or inject OrderService)
        return mapToDto(savedOrder);
    }

    private OrderResponseDTO mapToDto(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userFullName(order.getUser().getFullName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getRestaurantName())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .items(order.getOrderItems()
                        .stream()
                        .map(item -> OrderItemResponseDTO.builder()
                                .id(item.getId())
                                .menuItemId(item.getMenuItem().getId())
                                .itemName(item.getMenuItem().getItemName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .subtotal(item.getPrice().multiply(
                                        BigDecimal.valueOf(item.getQuantity())))
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
