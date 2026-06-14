package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    // Find all orders by customer id
    List<Order> findByUserId(Long userId);

    // Find all orders by restaurant id
    List<Order> findByRestaurantId(Long restaurantId);

    // Find PENDING orders older than X minutes
    // Used by scheduler to auto cancel
    // @Query annotation needed here:
    @Query("SELECT o FROM Order o WHERE " +
            "o.status = 'PENDING' AND " +
            "o.createdAt < :cutoffTime")
    List<Order> findPendingOrdersBefore(
            @Param("cutoffTime") LocalDateTime cutoffTime);
}
