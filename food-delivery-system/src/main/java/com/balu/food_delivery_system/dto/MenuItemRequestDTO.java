package com.balu.food_delivery_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequestDTO {

    @NotBlank(message = "Item name is required.")
    @Size(min = 3, message = "Item name must be greater than 3 characters.")
    private String itemName;

    @NotBlank(message = "Item Description is required.")
    private String itemDescription;

    @NotNull(message = "Price is required.")
    private BigDecimal price;

    @NotNull(message = "Is this vegetarian or not?")
    private boolean isVegetarian;

    @NotNull(message = "Category Id is required.")
    private Long categoryId;
}
