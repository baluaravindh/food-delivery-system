package com.balu.food_delivery_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotNull(message = "Menu item id is required.")
    private Long menuItemId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity should be atleast 1.")
    private Integer quantity;
}
