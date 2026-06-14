package com.balu.food_delivery_system.messaging.consumer;

import com.balu.food_delivery_system.dto.OrderNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationConsumer {

    // WHO: system (auto triggered by RabbitMQ)
    // WHAT to do: receive order notification,
    //             log it (simulates restaurant app)
    // WHAT to return: void
    @RabbitListener(queues = "${fds.rabbitmq.queue.order}")
    public void handleOrderNotification(OrderNotificationDTO dto) {

        log.info("[RESTAURANT NOTIFICATION] " +
                        "New order received! " +
                        "orderId={}, customer={}, " +
                        "restaurant={}, amount={}",
                dto.getOrderId(),
                dto.getUserFullName(),
                dto.getRestaurantName(),
                dto.getTotalAmount());
        // Real world → send to restaurant mobile app
        // Real world → send push notification
    }
}
