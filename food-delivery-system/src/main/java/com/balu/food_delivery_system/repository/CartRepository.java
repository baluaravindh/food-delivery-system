package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart by customer id
    Optional<Cart> findByUserId(Long userId);

    // Check if customer already has a cart
    boolean existsByUserId(Long userId);
}
