package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.OrderRequestDTO;
import com.balu.food_delivery_system.dto.OrderResponseDTO;
import com.balu.food_delivery_system.entity.Order;
import com.balu.food_delivery_system.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order",
        description = "Order management APIs")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    // Access: CUSTOMER only
    // Request: OrderRequestDTO
    // Response: 201 + OrderResponseDTO
    @Operation(summary = "Place Order")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            @Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(dto));
    }

    // GET /api/orders/{orderId}
    // Access: authenticated
    // Response: 200 + OrderResponseDTO
    @Operation(summary = "Get Order By Id")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // GET /api/orders/my-orders
    // Access: CUSTOMER only
    // Response: 200 + List<OrderResponseDTO>
    @Operation(summary = "Get My Orders")
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    // GET /api/orders/restaurant/{restaurantId}
    // Access: RESTAURANT_OWNER or ADMIN
    // Response: 200 + List<OrderResponseDTO>
    @Operation(summary = "Get Orders By Restaurant")
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    // PATCH /api/orders/{orderId}/status
    // Access: ADMIN or RESTAURANT_OWNER
    // Request: status string param
    // Response: 200 + OrderResponseDTO
    @Operation(summary = "Update Order Status")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // PATCH /api/orders/{orderId}/cancel
    // Access: CUSTOMER only
    // Response: 200 + OrderResponseDTO

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Cancel Order")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
