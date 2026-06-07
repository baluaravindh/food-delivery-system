package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.RestaurantRequestDTO;
import com.balu.food_delivery_system.dto.RestaurantResponseDTO;
import com.balu.food_delivery_system.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Restaurant", description = "Restaurant management APIs")
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    // POST /api/restaurants
    // Access: RESTAURANT_OWNER only
    // Request: RestaurantRequestDTO
    // Response: 201 + RestaurantResponseDTO
    @Operation(summary = "Create Restaurant")
    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@Valid @RequestBody RestaurantRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(dto));
    }

    // GET /api/restaurants
    // Access: ADMIN only
    // Response: 200 + List<RestaurantResponseDTO>
    @Operation(summary = "Get All Restaurants")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    // GET /api/restaurants/approved
    // Access: Any authenticated user
    // Response: 200 + List<RestaurantResponseDTO>
    @Operation(summary = "Get All Approved Restaurants")
    @GetMapping("/approved")
    public ResponseEntity<List<RestaurantResponseDTO>> getApprovedRestaurants() {
        return ResponseEntity.ok(restaurantService.getApprovedRestaurants());
    }

    // GET /api/restaurants/{id}
    // Access: Any authenticated user
    // Response: 200 + RestaurantResponseDTO
    @Operation(summary = "Get All Restaurants by Id")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // GET /api/restaurants/city/{city}
    // Access: Any authenticated user
    // Response: 200 + List<RestaurantResponseDTO>
    @Operation(summary = "Get All Restaurants by City")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<RestaurantResponseDTO>> getRestaurantsByCity(@PathVariable String city) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCity(city));
    }

    // PUT /api/restaurants/{id}
    // Access: RESTAURANT_OWNER only
    // Request: RestaurantRequestDTO
    // Response: 200 + RestaurantResponseDTO
    @Operation(summary = "Update Restaurant")
    @PutMapping("/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantRequestDTO dto) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(restaurantId, dto));
    }

    // PATCH /api/restaurants/{id}/approve
    // Access: ADMIN only
    // Request: approved boolean param
    // Response: 200 + RestaurantResponseDTO
    @Operation(summary = "Approve Restaurant")
    @PatchMapping("/{restaurantId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponseDTO> approveRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam boolean approved) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(restaurantId, approved));
    }

    // PATCH /api/restaurants/{id}/status
    // Access: RESTAURANT_OWNER only
    // Response: 200 + RestaurantResponseDTO
    @Operation(summary = "Toggle Restaurant Status")
    @PatchMapping("/{restaurantId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> toggleRestaurantStatus(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.toggleRestaurantStatus(restaurantId));
    }

    // POST /api/restaurants/{id}/image
    // Access: RESTAURANT_OWNER only
    // Request: MultipartFile image
    // Response: 200 + RestaurantResponseDTO
    @Operation(summary = "Upload Image")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(restaurantService.uploadImage(id, image));
    }
}
