package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    // Find all categories by restaurant id
    List<MenuCategory> findByRestaurant_Id(
            Long restaurantId);

    // Find active categories by restaurant id
    List<MenuCategory> findByRestaurant_IdAndIsActiveTrue(
            Long restaurantId);
}
