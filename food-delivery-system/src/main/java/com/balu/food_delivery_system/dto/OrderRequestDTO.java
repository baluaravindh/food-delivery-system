package com.balu.food_delivery_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotNull(message = "Restaurant id is required.")
    private Long restaurantId;

    @NotBlank(message = "Delivery address is required.")
    private String deliveryAddress;

    private String specialInstructions;

    @NotEmpty(message = "Items is required.")
    private List<OrderItemRequestDTO> items;
}
