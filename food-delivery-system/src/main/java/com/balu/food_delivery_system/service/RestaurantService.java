package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.RestaurantRequestDTO;
import com.balu.food_delivery_system.dto.RestaurantResponseDTO;
import com.balu.food_delivery_system.entity.Restaurant;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.exception.DuplicateUserFoundException;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.RestaurantRepository;
import com.balu.food_delivery_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    // WHO: RESTAURANT_OWNER only
    // WHAT to validate: owner doesn't already have a restaurant
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    @Transactional
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto) {

        //   Step 1: Get logged in user email from SecurityContextHolder
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email from userRepository throw ResourceNotFoundException if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        //   Step 3: Check if owner already has restaurant using restaurantRepository.existsByOwnerId()
        //           if yes → throw RuntimeException "You already have a restaurant registered"
        if (restaurantRepository.existsByUserId(user.getId())) {
            throw new DuplicateUserFoundException("You already have a restaurant registered");
        }

        //   Step 4: Build Restaurant entity using builder
        Restaurant restaurant = Restaurant.builder()
                .restaurantName(dto.getRestaurantName())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .city(dto.getCity())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .cuisineType(dto.getCuisineType())
                .openingTime(dto.getOpeningTime())
                .closingTime(dto.getClosingTime())
                .user(user)
                .isActive(true)
                .isApproved(false)
                .build();

        //   Step 5: Save restaurant to database
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        //   Step 6: log.info restaurant created with name and owner email
        log.info("Restaurant created with name {}", savedRestaurant.getUser().getFullName());

        //   Step 7: Return mapToDTO(savedRestaurant)
        return mapToDto(savedRestaurant);
    }

    // WHO: ADMIN only
    // WHAT to validate: nothing
    // WHAT to do:
    // WHAT to return: List<RestaurantResponseDTO>
    public List<RestaurantResponseDTO> getAllRestaurants() {

        //   Step 1: log.info "Fetching all restaurants"
        log.info("Fetching all restaurants");

        //   Step 2: restaurantRepository.findAll()
        //   Step 3: stream() and map each to DTO using this::mapToDTO
        //   Step 4: collect to List
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // WHO: any authenticated user
    // WHAT to validate: nothing
    // WHAT to do:
    // WHAT to return: List<RestaurantResponseDTO>
    public List<RestaurantResponseDTO> getApprovedRestaurants() {

        //   Step 1: log.info "Fetching approved restaurants"
        log.info("Fetching approved restaurants");

        //   Step 2: restaurantRepository .findByIsApprovedTrue()
        //   Step 3: stream() map to DTO collect to List
        return restaurantRepository.findByIsApprovedTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // WHO: any authenticated user
    // WHAT to validate: restaurant exists
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    public RestaurantResponseDTO getRestaurantById(Long restaurantId) {

        //   Step 1: restaurantRepository.findById(id)
        //   Step 2: orElseThrow ResourceNotFoundException "Restaurant not found: " + id
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        //   Step 3: Return mapToDTO(restaurant)
        return mapToDto(restaurant);
    }

    // WHO: any authenticated user
    // WHAT to validate: nothing
    // WHAT to do:
    // WHAT to return: List<RestaurantResponseDTO>
    public List<RestaurantResponseDTO> getRestaurantsByCity(String city) {

        //   Step 1: log.info "Fetching restaurants in city: {}" city
        log.info("Fetching restaurants in city: {}", city);

        //   Step 2: restaurantRepository.findByCityIgnoreCase(city)
        //   Step 3: stream() map to DTO collect to List
        return restaurantRepository.findByCityIgnoreCase(city)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - restaurant exists
    //   - logged in user is the owner
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    @Transactional
    public RestaurantResponseDTO updateRestaurant(Long restaurantId, RestaurantRequestDTO dto) {

        //   Step 1: Get logged in user email from SecurityContextHolder
        String loggedInEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find restaurant by id orElseThrow ResourceNotFoundException
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        //   Step 3: Validate owner
        //           if restaurant.getOwner().getEmail() does NOT equal logged in email
        //           throw RuntimeException "You are not the owner of this restaurant"
        if (!restaurant.getUser().getEmail().equals(loggedInEmail)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Update restaurant fields from dto
        //           set name, description, address
        //           set city, phone, email
        //           set cuisineType, openingTime, closingTime
        restaurant.setRestaurantName(dto.getRestaurantName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setAddress(dto.getAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setCuisineType(dto.getCuisineType());
        restaurant.setOpeningTime(dto.getOpeningTime());
        restaurant.setClosingTime(dto.getClosingTime());

        //   Step 5: Save updated restaurant
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        //   Step 6: log.info "Restaurant updated: {}" restaurant name
        log.info("Restaurant updated: {}", updatedRestaurant.getRestaurantName());

        //   Step 7: Return mapToDTO(updated)
        return mapToDto(updatedRestaurant);
    }

    // WHO: ADMIN only
    // WHAT to validate: restaurant exists
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    @Transactional
    public RestaurantResponseDTO approveRestaurant(Long restaurantId, boolean approved) {

        //   Step 1: Find restaurant by id orElseThrow ResourceNotFoundException
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        //   Step 2: Set restaurant.setIsApproved(approved)
        //   Step 3: Save restaurant
        restaurant.setApproved(approved);
        restaurantRepository.save(restaurant);

        //   Step 4: log.info "Restaurant {} approval status: {}" name, approved
        log.info("Restaurant {} approval status: {}", restaurant.getRestaurantName(), approved);

        //   Step 5: Return mapToDTO(saved)
        return mapToDto(restaurant);
    }

    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - restaurant exists
    //   - logged in user is the owner
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    @Transactional
    public RestaurantResponseDTO toggleRestaurantStatus(Long restaurantId) {

        //   Step 1: Get logged in user email
        //   Step 2: Find restaurant by id
        //           orElseThrow ResourceNotFoundException
        //   Step 3: Validate owner same as method 6
        String loggedEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        if (!restaurant.getUser().getEmail().equals(loggedEmail)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: Toggle isActive
        //           if currently true → set false
        //           if currently false → set true
        //           restaurant.setIsActive(!restaurant.isActive())
        restaurant.setActive(!restaurant.isActive());

        //   Step 5: Save restaurant
        restaurantRepository.save(restaurant);

        //   Step 6: log.info "Restaurant {} status: {}" name, isActive
        log.info("Restaurant {} status: {}", restaurant.getRestaurantName(), restaurant.isActive());

        //   Step 7: Return mapToDTO(saved)
        return mapToDto(restaurant);
    }

    // WHO: RESTAURANT_OWNER only
    // WHAT to validate:
    //   - restaurant exists
    //   - logged in user is the owner
    // WHAT to do:
    // WHAT to return: RestaurantResponseDTO
    @Transactional
    public RestaurantResponseDTO uploadImage(Long restaurantId, MultipartFile image)
            throws IOException {

        //   Step 1: Get logged in user email
        //   Step 2: Find restaurant by id orElseThrow ResourceNotFoundException
        //   Step 3: Validate owner same as method 6
        String loggedEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        if (!restaurant.getUser().getEmail().equals(loggedEmail)) {
            throw new RuntimeException("You are not the owner of this restaurant");
        }

        //   Step 4: If restaurant already has imageUrl
        //           delete old file using
        //           fileUploadService.deleteFile(restaurant.getImageUrl())
        if (restaurant.getImageUrl() != null) {
            fileUploadService.deleteFile(restaurant.getImageUrl());
        }

        //   Step 5: Upload new file using
        //           fileUploadService.uploadFile(image)
        //           store returned fileName
        String fileName = fileUploadService.uploadFile(image);

        //   Step 6: restaurant.setImageUrl(fileName)
        restaurant.setImageUrl(fileName);

        //   Step 7: Save restaurant
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        //   Step 8: log.info "Image uploaded for restaurant: {}" name
        log.info("Image uploaded for restaurant: {}", savedRestaurant.getRestaurantName());

        //   Step 9: Return mapToDTO(saved)
        return mapToDto(savedRestaurant);

    }

    //---MAPPER---
    private RestaurantResponseDTO mapToDto(Restaurant restaurant) {
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .restaurantName(restaurant.getRestaurantName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .cuisineType(restaurant.getCuisineType())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .imageUrl(restaurant.getImageUrl())
                .isActive(restaurant.isActive())
                .isApproved(restaurant.isApproved())
                .ownerId(restaurant.getUser().getId())
                .ownerName(restaurant.getUser().getFullName())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}
