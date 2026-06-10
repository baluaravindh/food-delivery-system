package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.CartItemRequestDTO;
import com.balu.food_delivery_system.dto.CartItemResponseDTO;
import com.balu.food_delivery_system.dto.CartResponseDTO;
import com.balu.food_delivery_system.entity.Cart;
import com.balu.food_delivery_system.entity.CartItem;
import com.balu.food_delivery_system.entity.MenuItem;
import com.balu.food_delivery_system.entity.User;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.repository.CartItemRepository;
import com.balu.food_delivery_system.repository.CartRepository;
import com.balu.food_delivery_system.repository.MenuItemRepository;
import com.balu.food_delivery_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final NotificationHelper notificationHelper;

    // METHOD 1: addToCart
    // WHO: CUSTOMER only
    // WHAT to validate:
    //   - menu item exists and is available
    //   - item belongs to approved restaurant
    // WHAT to return: CartResponseDTO
    @Transactional
    public CartResponseDTO addToCart(CartItemRequestDTO dto) {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email :" + email));

        //   Step 3: Find menuItem by id orElseThrow ResourceNotFoundException
        MenuItem item = menuItemRepository.findById(dto.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem not found with id :" + dto.getMenuItemId()));

        //   Step 4: Validate item is available
        //           if not → throw RuntimeException
        //           "This item is currently unavailable"
        if (!item.isAvailable()) {
            throw new RuntimeException("This item is currently unavailable");
        }

        //   Step 5: Validate restaurant is approved
        //           if not → throw RuntimeException
        //           "This restaurant is not available"
        if (!item.getCategory().getRestaurant().isApproved()) {
            throw new RuntimeException("This restaurant is not available");
        }

        //   Step 6: Find or create cart for customer
        //           if cart exists → use existing cart
        //           if not → create new Cart and save
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        //   Step 7: Check if same item already in cart
        //           findByCart_IdAndMenuItem_Id()
        //           if EXISTS → just increase quantity
        //                       existingItem.setQuantity(
        //                       existingItem.getQuantity()
        //                       + dto.getQuantity())
        //                       save cartItem
        //           if NOT EXISTS → create new CartItem
        //                           set cart, menuItem,
        //                           quantity from dto
        //                           save cartItem
        cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), dto.getMenuItemId())
                .ifPresentOrElse(existingItem -> {
                            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            CartItem cartItem = new CartItem();
                            cartItem.setCart(cart);
                            cartItem.setMenuItem(item);
                            cartItem.setQuantity(dto.getQuantity());
                            cartItemRepository.save(cartItem);
                        });

        //   Step 8: log.info "Item {} added to cart for customer: {}"
        log.info("Item {} added to cart for customer: {}",
                item.getItemName(), user.getEmail());

//        processCartNotification(user.getEmail(), item.getItemName());

        notificationHelper.sendCartUpdateNotification(user.getEmail(),
                item.getItemName(), dto.getQuantity());

        //   Step 9: Return getCart() to show full cart
        return getCart();
    }

    // METHOD 2: getCart
    // WHO: CUSTOMER only
    // WHAT to validate: nothing
    // WHAT to return: CartResponseDTO
    public CartResponseDTO getCart() {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email :" + email));

        //   Step 3: Find cart by customerId
        //           if no cart → return empty CartResponseDTO
        //             with empty list and zero totals
        //   Step 4: Map cart items to CartItemResponseDTO
        //   Step 5: Calculate totalAmount
        //           sum of (price × quantity) for each item
        //   Step 6: Build and return CartResponseDTO
        //           set cartId, customerId, customerName
        //           set items list
        //           set totalItems = items.size()
        //           set totalAmount
        return cartRepository.findByUserId(user.getId())
                .map(this::mapToDto)
                .orElse(CartResponseDTO.builder()
                        .userId(user.getId())
                        .userFullName(user.getFullName())
                        .items(new ArrayList<>())
                        .totalItems(0)
                        .totalAmount(BigDecimal.ZERO)
                        .build());
    }

    // METHOD 3: updateCartItem
    // WHO: CUSTOMER only
    // WHAT to validate:
    //   - cart item exists
    //   - item belongs to logged in customer's cart

    // WHAT to return: CartResponseDTO
    @Transactional
    public CartResponseDTO updateCartItem(Long cartItemId, CartItemRequestDTO dto) {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email :" + email));

        //   Step 3: Find cartItem by cartItemId orElseThrow ResourceNotFoundException
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart Item not found with id :" + cartItemId));

        //   Step 4: Validate item belongs to customer
        //           cartItem.getCart().getCustomer()
        //           .getEmail() must equal logged in email
        //           throw RuntimeException if not
        if (!cartItem.getCart().getUser().getEmail().equals(user.getEmail())) {
            throw new ResourceNotFoundException("This user is not the cart");
        }

        //   Step 5: Update quantity cartItem.setQuantity(dto.getQuantity())
        //   Step 6: Save cartItem
        cartItem.setQuantity(dto.getQuantity());
        cartItemRepository.save(cartItem);

        //   Step 7: log.info "Cart item updated"
        log.info("Cart item updated");

        //   Step 8: Return getCart()
        return getCart();
    }

    // METHOD 4: removeCartItem
    // WHO: CUSTOMER only
    // WHAT to validate:
    //   - cart item exists
    //   - item belongs to logged in customer's cart
    // WHAT to return: CartResponseDTO
    @Transactional
    public CartResponseDTO removeCartItem(Long cartItemId) {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email :" + email));

        //   Step 3: Find cartItem by cartItemId orElseThrow ResourceNotFoundException
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart Item not found with id :" + cartItemId));

        //   Step 4: Validate item belongs to customer
        if (!cartItem.getCart().getUser().getEmail().equals(user.getEmail())) {
            throw new ResourceNotFoundException("This user is not the cart");
        }

        //   Step 5: Delete cartItem cartItemRepository.delete(cartItem)
        cartItemRepository.delete(cartItem);

        //   Step 6: log.info "Cart item removed"
        log.info("Cart item removed");

        //   Step 7: Return getCart()
        return getCart();
    }

    // METHOD 5: clearCart
    // WHO: CUSTOMER only
    // WHAT to validate: cart exists
    // WHAT to return: CartResponseDTO
    @Transactional
    public CartResponseDTO clearCart() {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email :" + email));

        //   Step 3: Find cart by customerId
        //           orElseThrow ResourceNotFoundException
        //           "No cart found"
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No cart found"));

        //   Step 4: Delete all items in cart
        //           cartItemRepository
        //           .deleteByCart_Id(cart.getId())
        cartItemRepository.deleteByCart_Id(cart.getId());

        //   Step 5: log.info "Cart cleared for: {}"
        log.info("Cart cleared for: {}", user.getEmail());

        //   Step 6: Return getCart() (will return empty cart)
        return getCart();
    }

    //---MAPPER---
    private CartResponseDTO mapToDto(Cart cart) {

        BigDecimal total = BigDecimal.ZERO;

        List<CartItemResponseDTO> itemDTOs = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal subTotal = cartItem.getMenuItem().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subTotal);

            itemDTOs.add(new CartItemResponseDTO(
                    cartItem.getId(),
                    cartItem.getMenuItem().getId(),
                    cartItem.getMenuItem().getItemName(),
                    cartItem.getMenuItem().getItemDescription(),
                    cartItem.getMenuItem().getPrice(),
                    cartItem.getQuantity(),
                    subTotal,
                    cartItem.getMenuItem().getCategory().getRestaurant().getId(),
                    cartItem.getMenuItem().getCategory().getRestaurant().getRestaurantName(),
                    cartItem.getMenuItem().isVegetarian()
            ));
        }
        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .userFullName(cart.getUser().getFullName())
                .items(itemDTOs)
                .totalItems(itemDTOs.size())
                .totalAmount(total)
                .createdAt(cart.getCreatedAt())
                .build();
    }

    //---ASYNC METHOD---
//    @Async
//    public void processCartNotification(String userEmail, String itemName) {
//        log.info("Background: Cart updated for {} " + "- item: {}", userEmail, itemName);
//    }
}
