package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Find all items by category id
    List<MenuItem> findByCategory_Id(Long categoryId);

    // Find available items by category id
    List<MenuItem> findByCategory_IdAndIsAvailableTrue(Long categoryId);

    // Find all items by restaurant id
    // through category
    List<MenuItem> findByCategoryRestaurantId(Long restaurantId);

    // Find available items by restaurant id
    List<MenuItem> findByCategory_RestaurantIdAndIsAvailableTrue(Long restaurantId);
}
