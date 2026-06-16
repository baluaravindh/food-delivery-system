package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.entity.Order;
import com.balu.food_delivery_system.messaging.publisher.OrderEventPublisher;
import com.balu.food_delivery_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
        List<Order> pendingOrders = orderRepository.findPendingOrdersBefore(cutoffTime, Order.OrderStatus.PENDING);

        if (pendingOrders.isEmpty()) {
            log.info("[SCHEDULER] No unpaid" + " orders found.");
            return;
        }

        // Cancel each pending order
        for (Order order : pendingOrders) {
            cancelSingleOrder(order);
        }
        log.info("[SCHEDULER] {} orders cancelled", pendingOrders.size());

    }

    @Async("taskExecutor")
    public void cancelSingleOrder(Order order) {

        //   Step 1: Set order status to CANCELLED
        order.setStatus(Order.OrderStatus.CANCELLED);

        //   Step 2: Save order to repository
        orderRepository.save(order);

        //   Step 3: Publish cancellation event to Kafka
        orderEventPublisher.publishOrderStatusEvent(order.getId(), "CANCELLED");

        //   Step 4: log.info order id and current thread name
        //           Thread.currentThread().getName()
        //           helps verify different threads in logs
        log.info("[SCHEDULER] Order {} auto-cancelled on thread: {}",
                order.getId(), Thread.currentThread().getName());
    }
}
