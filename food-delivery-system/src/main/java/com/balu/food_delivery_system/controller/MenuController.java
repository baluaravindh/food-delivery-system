package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.MenuCategoryRequestDTO;
import com.balu.food_delivery_system.dto.MenuCategoryResponseDTO;
import com.balu.food_delivery_system.dto.MenuItemRequestDTO;
import com.balu.food_delivery_system.dto.MenuItemResponseDTO;
import com.balu.food_delivery_system.service.MenuService;
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

@Tag(name = "Menu", description = "Menu management APIs")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    // POST /api/menu/categories
    // Access: RESTAURANT_OWNER only
    // Request: MenuCategoryRequestDTO
    // Response: 201 + MenuCategoryResponseDTO
    @Operation(summary = "Create Menu Category")
    @PostMapping("/categories")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuCategoryResponseDTO> createCategory(
            @Valid @RequestBody MenuCategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(dto));
    }

    // GET /api/menu/categories/{restaurantId}
    // Access: any authenticated user
    // Response: 200 + List<MenuCategoryResponseDTO>
    @Operation(summary = "Get Categories By Restaurant")
    @GetMapping("/categories/{restaurantId}")
    public ResponseEntity<List<MenuCategoryResponseDTO>> getCategoriesByRestaurant(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getCategoriesByRestaurant(restaurantId));
    }

    // PATCH /api/menu/categories/{categoryId}/status
    // Access: RESTAURANT_OWNER only
    // Response: 200 + MenuCategoryResponseDTO
    @Operation(summary = "Toggle Category Status")
    @PatchMapping("categories/{categoryId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuCategoryResponseDTO> toggleCategoryStatus(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.toggleCategoryStatus(categoryId));
    }

    // POST /api/menu/items
    // Access: RESTAURANT_OWNER only
    // Request: MenuItemRequestDTO
    // Response: 201 + MenuItemResponseDTO
    @Operation(summary = "Add Menu Item")
    @PostMapping("/items")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> addMenuItem(
            @Valid @RequestBody MenuItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.addMenuItem(dto));
    }

    // GET /api/menu/items/category/{categoryId}
    // Access: any authenticated user
    // Response: 200 + List<MenuItemResponseDTO>
    @Operation(summary = "Get Menu Items By Category")
    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuItemsByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getMenuItemsByCategory(categoryId));
    }

    // GET /api/menu/items/restaurant/{restaurantId}
    // Access: any authenticated user
    // Response: 200 + List<MenuItemResponseDTO>
    @Operation(summary = "Get Menu By Restaurant")
    @GetMapping("/items/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuByRestaurant(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuByRestaurant(restaurantId));
    }

    // PUT /api/menu/items/{itemId}
    // Access: RESTAURANT_OWNER only
    // Request: MenuItemRequestDTO
    // Response: 200 + MenuItemResponseDTO
    @Operation(summary = "Update Menu Item")
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequestDTO dto) {
        return ResponseEntity.ok(menuService.updateMenuItem(itemId, dto));
    }

    // PATCH /api/menu/items/{itemId}/availability
    // Access: RESTAURANT_OWNER only
    // Response: 200 + MenuItemResponseDTO
    @Operation(summary = "Toggle Item Availability")
    @PatchMapping("/items/{itemId}/availability")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> toggleItemAvailability(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.toggleItemAvailability(itemId));
    }

    // POST /api/menu/items/{itemId}/image
    // Access: RESTAURANT_OWNER only
    // consumes: MULTIPART_FORM_DATA
    // Request: MultipartFile image
    // Response: 200 + MenuItemResponseDTO
    @Operation(summary = "Upload Menu Item Image")
    @PostMapping(value = "items/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> uploadMenuItemImage(
            @PathVariable Long itemId,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(menuService.uploadMenuItemImage(itemId, image));
    }
}
