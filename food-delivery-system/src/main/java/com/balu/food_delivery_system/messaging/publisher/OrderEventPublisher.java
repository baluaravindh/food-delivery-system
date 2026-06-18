package com.balu.food_delivery_system.messaging.publisher;

import com.balu.food_delivery_system.dto.OrderNotificationDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${fds.rabbitmq.exchange}")
    private String exchange;

    @Value("${fds.rabbitmq.routing.key.order}")
    private String orderRoutingKey;

    @Value("${fds.kafka.topic.order-status}")
    private String orderStatusTopic;

    // WHO: system after order placed
    // WHAT to do: publish order notification
    //             to RabbitMQ exchange
    //             restaurant will consume this
    // WHAT to return: void
    @Async
    public void publishOrderToRestaurant(OrderNotificationDTO dto) {

        try {
            amqpTemplate.convertAndSend(
                    exchange,
                    orderRoutingKey,
                    dto);
            log.info("Order notification sent" +
                    " to restaurant via RabbitMQ:" +
                    " orderId={}", dto.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order" +
                            " to RabbitMQ: {}",
                    e.getMessage());
        }
    }

    // WHO: system on any status change
    // WHAT to do: publish status event to Kafka
    //             for real time order tracking
    // WHAT to return: void
    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "publishOrderStatusEventFallback")
    public void publishOrderStatusEvent(Long orderId, String status) {

        //   Step 1: Build JSON message string
        //  include orderId, status, timestamp
        String message = String.format(
                "{\"orderId\":%d," +
                        "\"status\":\"%s\"," +
                        "\"timestamp\":\"%s\"}",
                orderId, status,
                LocalDateTime.now());

        //   Step 2: Send to Kafka topic
        //   kafkaTemplate.send(topic, key, message)
        kafkaTemplate.send(
                orderStatusTopic,
                String.valueOf(orderId),
                message);

        //   Step 3: log.info published successfully
        //  (remove the try/catch — circuit breaker handles failures)
        log.info("Order status event" +
                        " published to Kafka:" +
                        " orderId={}, status={}",
                orderId, status);
    }

    public void publishOrderStatusEventFallback(Long orderId, String status, Exception e) {

        //   Step 1: log.warn circuit breaker triggered
        //           include orderId, status, exception message
        //           e.g. "[CIRCUIT BREAKER] Kafka publish failed for orderId: {}"
        log.warn("[CIRCUIT BREAKER] Kafka publish failed for orderId: {}, status: {}, errorMessage: {}"
                , orderId, status, e.getMessage());

        //   Step 2: (optional) store failed event somewhere for retry later
        //           for now, just log — don't throw exception
    }
}
