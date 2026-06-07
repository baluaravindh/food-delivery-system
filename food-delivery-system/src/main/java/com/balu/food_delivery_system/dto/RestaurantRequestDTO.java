package com.balu.food_delivery_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RestaurantRequestDTO {

    @NotBlank(message = "Restaurant name is required.")
    @Size(min = 6, message = "Restaurant name contains atleast 6 characters.")
    private String restaurantName;

    @NotBlank(message = "Description is required.")
    @Size(min = 6, max = 100, message = "Description must be between 6 to 100 characters.")
    private String description;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Cuisine type is required.")
    private String cuisineType;

    @NotNull(message = "Opening time is required.")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required.")
    private LocalTime closingTime;
}
