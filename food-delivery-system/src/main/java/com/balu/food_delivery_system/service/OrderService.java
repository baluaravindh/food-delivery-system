package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.dto.*;
import com.balu.food_delivery_system.entity.*;
import com.balu.food_delivery_system.exception.ResourceNotFoundException;
import com.balu.food_delivery_system.messaging.publisher.OrderEventPublisher;
import com.balu.food_delivery_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EmailService emailService;
    private final OrderEventPublisher eventPublisher;

    // METHOD 1: placeOrder
    // WHO: CUSTOMER only
    // WHAT to validate:
    //   - restaurant exists and is approved
    //   - items list not empty
    //   - each menu item exists and is available
    //   - item belongs to same restaurant
    // WHAT to return: OrderResponseDTO
    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO dto) {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        //   Step 3: Find restaurant by id
        //           validate isApproved = true
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + dto.getRestaurantId()));
        if (!restaurant.isApproved()) {
            throw new RuntimeException("Restaurant is not available.");
        }

        //   Step 4: Build Order object
        //           set customer, restaurant,
        //           deliveryAddress, specialInstructions
        //           status = PENDING (via @PrePersist)
        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .deliveryAddress(dto.getDeliveryAddress())
                .specialInstructions(dto.getSpecialInstructions())
                .status(Order.OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        //   Step 5: Process each item in dto.getItems()
        //           - find menuItem by id
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            // Find menuItem by id from DTO
            MenuItem menuItem = menuItemRepository
                    .findById(itemDto.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "MenuItem not found: " + itemDto.getMenuItemId()));

            //           - validate isAvailable
            if (!menuItem.isAvailable()) {
                throw new RuntimeException("Order is not available.");
            }

            //           - validate item.category.restaurant
            //             matches order restaurant
            if (!menuItem.getCategory().getRestaurant().getId().equals(dto.getRestaurantId())) {
                throw new RuntimeException("Restaurant is not available.");
            }

            //           - create OrderItem
            //             set order, menuItem, quantity
            //             set priceAtOrder = menuItem.getPrice()
            //           - add to order.getOrderItems()
            //           - add to totalAmount

            // Create OrderItem
            OrderItems orderItem = OrderItems.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemDto.getQuantity())
                    .price(menuItem.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);

            // Add to total
            BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(
                            itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }


        //   Step 6: Set order.setTotalAmount(totalAmount)
        order.setTotalAmount(totalAmount);

        //   Step 7: Save order (cascade saves items too)
        Order savedOrder = orderRepository.save(order);

        //   Step 8: Clear customer cart
        //           (order placed = cart cleared)
        cartRepository.findByUserId(user.getId())
                .ifPresent(cart -> {
                    cartItemRepository.deleteByCart_Id(cart.getId());
                    cartItemRepository.flush();
                });

        //   Step 9: Send confirmation email ASYNC
        //           emailService.sendOrderConfirmationEmail()
        emailService.sendOrderConfirmationEmail(
                user.getEmail(), user.getFullName(), order.getId(), order.getTotalAmount());

        //   Step 10: Publish to RabbitMQ ASYNC
        //            eventPublisher
        //            .publishOrderToRestaurant()
        OrderNotificationDTO notification = OrderNotificationDTO.builder()
                .orderId(savedOrder.getId())
                .userFullName(user.getFullName())
                .restaurantName(restaurant.getRestaurantName())
                .restaurantId(restaurant.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .deliveryAddress(dto.getDeliveryAddress())
                .itemCount(dto.getItems().size())
                .status("PENDING")
                .build();

        eventPublisher.publishOrderToRestaurant(notification);

        //   Step 11: Publish to Kafka ASYNC
        //            eventPublisher
        //            .publishOrderStatusEvent(
        //                orderId, "PENDING")
        eventPublisher.publishOrderStatusEvent(order.getId(), "PENDING");

        //   Step 12: log.info order placed
        log.info("Order placed successfully: orderId={}", savedOrder.getId());

        //   Step 13: Return mapToDTO(savedOrder)
        return mapToDto(savedOrder);
    }

    // METHOD 2: getOrderById
    // WHO: CUSTOMER or ADMIN
    // WHAT to validate: order exists
    // WHAT to return: OrderResponseDTO
    public OrderResponseDTO getOrderById(Long orderId) {

        //   Step 1: Find order by id
        //           orElseThrow ResourceNotFoundException
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        //   Step 2: Return mapToDTO(order)
        return mapToDto(order);
    }

    // METHOD 3: getMyOrders
    // WHO: CUSTOMER only
    // WHAT to validate: nothing
    // WHAT to return: List<OrderResponseDTO>
    public List<OrderResponseDTO> getMyOrders() {

        //   Step 1: Get logged in user email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        //   Step 3: findByCustomerId(user.getId())
        //   Step 4: stream() map to DTO collect to List
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD 4: getOrdersByRestaurant
    // WHO: RESTAURANT_OWNER or ADMIN
    // WHAT to validate: restaurant exists
    // WHAT to return: List<OrderResponseDTO>
    public List<OrderResponseDTO> getOrdersByRestaurant(Long restaurantId) {

        //   Step 1: Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        //   Step 2: findByRestaurantId(restaurantId)
        //   Step 3: stream() map to DTO collect to List
        return orderRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD 5: updateOrderStatus
    // WHO: ADMIN or RESTAURANT_OWNER
    // WHAT to validate:
    //   - order exists
    //   - status transition is valid
    //     CANCELLED order cannot be updated
    //     DELIVERED order cannot be updated
    // WHAT to return: OrderResponseDTO
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {

        //   Step 1: Find order by id
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        //   Step 2: Validate current status
        //           if CANCELLED → throw exception
        //           if DELIVERED → throw exception
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is cancelled.");
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Order is already delivered.");
        }

        //   Step 3: Set new status
        order.setStatus(Order.OrderStatus.valueOf(status));

        //   Step 4: Save order
        Order savedOrder = orderRepository.save(order);

        //   Step 5: Publish status event to Kafka
        //           eventPublisher
        //           .publishOrderStatusEvent()
        eventPublisher.publishOrderStatusEvent(savedOrder.getId(), status);

        //   Step 6: log.info status updated
        log.info("Order updated successfully {}");

        //   Step 7: Return mapToDTO(saved)
        return mapToDto(savedOrder);
    }

    // METHOD 6: cancelOrder
    // WHO: CUSTOMER only (their own order)
    // WHAT to validate:
    //   - order exists
    //   - order belongs to logged in customer
    //   - order is still PENDING
    //     (cannot cancel CONFIRMED or beyond)
    // WHAT to return: OrderResponseDTO
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {

        //   Step 1: Get logged in email
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        //   Step 2: Find order by id
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        //   Step 3: Validate ownership
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to cancel this order.");
        }

        //   Step 4: Validate status is PENDING
        //           throw exception if not
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled as it is already " + order.getStatus());
        }

        //   Step 5: Set status = CANCELLED
        order.setStatus(Order.OrderStatus.CANCELLED);

        //   Step 6: Save order
        Order savedOrder = orderRepository.save(order);

        //   Step 7: Publish to Kafka
        eventPublisher.publishOrderStatusEvent(order.getId(), "CANCELLED");

        //   Step 8: Return mapToDTO(saved)
        return mapToDto(savedOrder);
    }

    private OrderResponseDTO mapToDto(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userFullName(order.getUser().getFullName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getRestaurantName())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .items(order.getOrderItems()
                        .stream()
                        .map(item -> OrderItemResponseDTO.builder()
                                .id(item.getId())
                                .menuItemId(item.getMenuItem().getId())
                                .itemName(item.getMenuItem().getItemName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .subtotal(item.getPrice().multiply(
                                        BigDecimal.valueOf(item.getQuantity())))
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
