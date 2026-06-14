package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems,Long> {

    // Find all items by order id
    List<OrderItems> findByOrderId(Long orderId);
}
