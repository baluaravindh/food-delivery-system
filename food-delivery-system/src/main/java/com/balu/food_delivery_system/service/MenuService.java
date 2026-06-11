package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.MenuCategoryRequestDTO;
import com.balu.food_delivery_system.dto.MenuCategoryResponseDTO;
import com.balu.food_delivery_system.dto.MenuItemRequestDTO;
import com.balu.food_delivery_system.dto.MenuItemResponseDTO;
import com.balu.food_delivery_system.entity.MenuCategory;
import com.balu.food_delivery_system.entity.MenuItem;
import com.balu.food_delivery_system.entity.Restaurant;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.MenuCategoryRepository;
import com.balu.food_delivery_system.repository.MenuItemRepository;
import com.balu.food_delivery_system.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final FileUploadService fileUploadService;

    // METHOD 1: createCategory
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - restaurant exists
    //   - logged in user is the owner
    // WHAT to return: MenuCategoryResponseDTO

    @Transactional
    public MenuCategoryResponseDTO createCategory(MenuCategoryRequestDTO dto) {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find restaurant by restaurantId orElseThrow ResourceNotFoundException
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id " + dto.getRestaurantId()));

        //   Step 3: Validate owner — logged in email
        //           must match restaurant owner email
        //           throw RuntimeException if not owner
        if (!restaurant.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Build MenuCategory using builder
        //           set categoryName from dto
        //           set description from dto
        //           set restaurant
        //           set isActive = true
        MenuCategory menuCategory = MenuCategory.builder()
                .categoryName(dto.getCategoryName())
                .categoryDescription(dto.getCategoryDescription())
                .restaurant(restaurant)
                .isActive(true)
                .build();

        //   Step 5: Save category
        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);

        //   Step 6: log.info "Category created: {} for restaurant: {}"
        log.info("Category created: {} for restaurant: {}");

        //   Step 7: Return mapCategoryToDTO(saved)
        return mapCategoryToDTO(savedCategory);
    }

    // METHOD 2: getCategoriesByRestaurant
    // WHO: any authenticated user
    // WHAT to validate: restaurant exists
    // WHAT to return: List<MenuCategoryResponseDTO>
    public List<MenuCategoryResponseDTO> getCategoriesByRestaurant(Long restaurantId) {

        //   Step 1: Validate restaurant exists
        //           orElseThrow ResourceNotFoundException
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id " + restaurantId));

        //   Step 2: findByRestaurantIdAndIsActiveTrue()
        //   Step 3: stream() map to DTO collect to List
        return menuCategoryRepository.findByRestaurant_IdAndIsActiveTrue(restaurantId)
                .stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    // METHOD 3: toggleCategoryStatus
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - category exists
    //   - logged in user is restaurant owner
    // WHAT to return: MenuCategoryResponseDTO
    @Transactional
    public MenuCategoryResponseDTO toggleCategoryStatus(Long categoryId) {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find category by id orElseThrow ResourceNotFoundException
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));

        //   Step 3: Validate owner
        if (!menuCategory.getRestaurant().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Toggle isActive
        //           category.setIsActive(!category.isActive())
        menuCategory.setActive(!menuCategory.isActive());

        //   Step 5: Save and log
        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);

        //   Step 6: Return mapCategoryToDTO(saved)
        return mapCategoryToDTO(savedCategory);
    }

    // METHOD 4: addMenuItem
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - category exists
    //   - logged in user is restaurant owner
    // WHAT to return: MenuItemResponseDTO
    @CacheEvict(value = "menuByRestaurant", key = "#dto.categoryId")
    @Transactional
    public MenuItemResponseDTO addMenuItem(MenuItemRequestDTO dto) {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find category by categoryId orElseThrow ResourceNotFoundException
        MenuCategory category = menuCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id " + dto.getCategoryId()));

        //   Step 3: Validate owner
        //           category.getRestaurant().getUser()
        //           .getEmail() must equal logged in email
        if (!category.getRestaurant().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Build MenuItem using builder
        //           set itemName from dto
        //           set description from dto
        //           set price from dto
        //           set isVegetarian from dto
        //           set category
        //           set isAvailable = true
        MenuItem item = MenuItem.builder()
                .itemName(dto.getItemName())
                .itemDescription(dto.getItemDescription())
                .price(dto.getPrice())
                .isVegetarian(dto.isVegetarian())
                .category(category)
                .isAvailable(true)
                .build();

        //   Step 5: Save item
        MenuItem savedItem = menuItemRepository.save(item);

        //   Step 6: log.info "Menu item added: {} to category: {}"
        log.info("Menu item added: {} to category: {}");

        //   Step 7: Return mapItemToDTO(saved)
        return mapItemToDTO(savedItem);
    }

    // METHOD 5: getMenuItemsByCategory
    // WHO: any authenticated user
    // WHAT to validate: category exists
    // WHAT to return: List<MenuItemResponseDTO>
    public List<MenuItemResponseDTO> getMenuItemsByCategory(Long categoryId) {

        //   Step 1: Validate category exists
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id " + categoryId));

        //   Step 2: findByCategoryIdAndIsAvailableTrue()
        //   Step 3: stream() map to DTO collect to List
        return menuItemRepository.findByCategory_IdAndIsAvailableTrue(categoryId)
                .stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());
    }

    // METHOD 6: getMenuByRestaurant
    // WHO: any authenticated user
    // WHAT to validate: restaurant exists
    // WHAT to return: List<MenuItemResponseDTO>
    @Cacheable(value = "MenuByRestaurant", key = "#restaurantId")
    public List<MenuItemResponseDTO> getMenuByRestaurant(Long restaurantId) {

        //   Step 1: Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id " + restaurantId));

        //   Step 2: findByCategoryRestaurantIdAndIsAvailableTrue()
        //   Step 3: stream() map to DTO collect to List
        return menuItemRepository.findByCategory_RestaurantIdAndIsAvailableTrue(restaurantId)
                .stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());
    }

    // METHOD 7: updateMenuItem
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - item exists
    //   - logged in user is restaurant owner
    // WHAT to return: MenuItemResponseDTO
    @CacheEvict(value = "menuByRestaurant", allEntries = true)
    @Transactional
    public MenuItemResponseDTO updateMenuItem(Long itemId, MenuItemRequestDTO dto) {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find item by id orElseThrow ResourceNotFoundException
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + itemId));

        //   Step 3: Validate owner
        //           item.getCategory().getRestaurant()
        //           .getUser().getEmail()
        if (!item.getCategory().getRestaurant().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Update fields on existing item
        //           set itemName, description,
        //           price, isVegetarian
        item.setItemName(dto.getItemName());
        item.setItemDescription(dto.getItemDescription());
        item.setPrice(dto.getPrice());
        item.setVegetarian(dto.isVegetarian());

        //   Step 5: Save and log
        MenuItem savedItem = menuItemRepository.save(item);
        log.info("Menu item updated: {} to item: {}");

        //   Step 6: Return mapItemToDTO(saved)
        return mapItemToDTO(savedItem);
    }

    // METHOD 8: toggleItemAvailability
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - item exists
    //   - logged in user is owner
    // WHAT to return: MenuItemResponseDTO
    @CacheEvict(value = "menuByRestaurant", allEntries = true)
    @Transactional
    public MenuItemResponseDTO toggleItemAvailability(Long itemId) {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find item by id
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + itemId));

        //   Step 3: Validate owner
        if (!item.getCategory().getRestaurant().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Toggle isAvailable item.setIsAvailable(!item.isAvailable())
        item.setAvailable(!item.isAvailable());

        //   Step 5: Save and log
        MenuItem savedItem = menuItemRepository.save(item);
        log.info("Menu item updated: {} to item: {}");

        //   Step 6: Return mapItemToDTO(saved)
        return mapItemToDTO(savedItem);
    }

    // METHOD 9: uploadMenuItemImage
    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - item exists
    //   - logged in user is owner
    // WHAT to return: MenuItemResponseDTO
    public MenuItemResponseDTO uploadMenuItemImage(
            Long itemId, MultipartFile image) throws IOException {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find item by id
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + itemId));

        //   Step 3: Validate owner
        if (!item.getCategory().getRestaurant().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Delete old image if exists
        if (item.getImageUrl() != null) {
            fileUploadService.deleteFile(item.getImageUrl());
        }

        //   Step 5: Upload new image
        //           String fileName = fileUploadServie.uploadFile(image)
        String fileName = fileUploadService.uploadFile(image);

        //   Step 6: item.setImageUrl(fileName)
        item.setImageUrl(fileName);

        //   Step 7: Save and log
        MenuItem savedItem = menuItemRepository.save(item);
        log.info("Menu item image uploaded: {} to item: {}");

        //   Step 8: Return mapItemToDTO(saved)
        return mapItemToDTO(savedItem);
    }

    //---MAPPER---
    private MenuItemResponseDTO mapItemToDTO(MenuItem item) {
        return MenuItemResponseDTO.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .price(item.getPrice())
                .isVegetarian(item.isVegetarian())
                .isAvailable(item.isAvailable())
                .imageUrl(item.getImageUrl())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getCategoryName())
                .restaurantId(item.getCategory().getRestaurant().getId())
                .restaurantName(item.getCategory().getRestaurant().getRestaurantName())
                .createdAt(item.getCreatedAt())
                .build();

    }

    //---MAPPER
    private MenuCategoryResponseDTO mapCategoryToDTO(MenuCategory category) {
        return MenuCategoryResponseDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .categoryDescription(category.getCategoryDescription())
                .isActive(category.isActive())
                .restaurantId(category.getRestaurant().getId())
                .restaurantName(category.getRestaurant().getRestaurantName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
