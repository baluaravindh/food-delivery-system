package com.balu.food_delivery_system.repository;

import com.balu.food_delivery_system.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find cart item by cart id and menu item id
    // Used to check if item already in cart
    Optional<CartItem> findByCart_IdAndMenuItem_Id(
            Long cartId, Long menuItemId);

    // Delete all items by cart id
    void deleteByCart_Id(Long cartId);
}
