package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.entity.Order;
import com.balu.food_delivery_system.messaging.publisher.OrderEventPublisher;
import com.balu.food_delivery_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSchedulerService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    // WHO: system (runs automatically)
    // WHAT to do: every 1 minute check for
    //             PENDING orders older than 10 mins
    //             cancel them automatically
    //             publish cancellation event to Kafka
    // WHAT to return: void
    @Scheduled(fixedRate = 60000)
    public void cancelUnpaidOrders() {

        log.info("[SCHEDULER] Checking for" + " unpaid orders...");

        // Calculate cutoff time — 10 mins ago
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);

        // Find all PENDING orders older than cutoff
        List<Order> pendingOrders = orderRepository.findPendingOrdersBefore(cutoffTime);

        if (pendingOrders.isEmpty()) {
            log.info("[SCHEDULER] No unpaid" + " orders found.");
            return;
        }

        // Cancel each pending order
        for (Order order : pendingOrders) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Publish cancellation to Kafka
            orderEventPublisher.publishOrderStatusEvent(order.getId(), "CANCELLED");

            log.info("[SCHEDULER] Order {} " + "auto-cancelled due to" + " non-payment", order.getId());
        }
        log.info("[SCHEDULER] {} orders cancelled", pendingOrders.size());

    }
}
