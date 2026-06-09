package com.balu.food_delivery_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuCategoryRequestDTO {

    @NotBlank(message = "Category name is required.")
    @Size(min = 4, message = "Name must be greater than 4 characters.")
    private String categoryName;

    //    @NotBlank(message = "Category Description is required.")
    private String categoryDescription;

    @NotNull(message = "Restaurant Id is required.")
    private Long restaurantId;
}
