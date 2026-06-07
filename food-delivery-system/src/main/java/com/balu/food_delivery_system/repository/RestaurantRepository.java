package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find all approved restaurants
    List<Restaurant> findByIsApprovedTrue();

    // Find all restaurants by city
    List<Restaurant> findByCityIgnoreCase(String city);

    // Check if owner already has restaurant
    List<Restaurant> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
