package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.CartItemRequestDTO;
import com.balu.food_delivery_system.dto.CartResponseDTO;
import com.balu.food_delivery_system.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Cart management APIs")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    // POST /api/cart/add
    // Access: CUSTOMER only
    // Request: CartItemRequestDTO
    // Response: 200 + CartResponseDTO
    @Operation(summary = "Add To Cart")
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDTO> addToCart(@Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(dto));
    }

    // GET /api/cart
    // Access: CUSTOMER only
    // Response: 200 + CartResponseDTO
    @Operation(summary = "Get Cart")
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    // PUT /api/cart/items/{cartItemId}
    // Access: CUSTOMER only
    // Request: CartItemRequestDTO (new quantity)
    // Response: 200 + CartResponseDTO
    @Operation(summary = "Update Cart Item")
    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.updateCartItem(cartItemId, dto));
    }

    // DELETE /api/cart/items/{cartItemId}
    // Access: CUSTOMER only
    // Response: 200 + CartResponseDTO
    @Operation(summary = "Remove Cart Item")
    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDTO> removeCartItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeCartItem(cartItemId));
    }

    // DELETE /api/cart/clear
    // Access: CUSTOMER only
    // Response: 200 + CartResponseDTO
    @Operation(summary = "Clear Cart")
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDTO> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
