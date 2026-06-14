package com.balu.food_delivery_system.messaging.publisher;

import com.balu.food_delivery_system.dto.OrderNotificationDTO;
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
    @Async
    public void publishOrderStatusEvent(Long orderId, String status) {

        try {
            String message = String.format(
                    "{\"orderId\":%d," +
                            "\"status\":\"%s\"," +
                            "\"timestamp\":\"%s\"}",
                    orderId, status,
                    LocalDateTime.now());

            kafkaTemplate.send(
                    orderStatusTopic,
                    String.valueOf(orderId),
                    message);

            log.info("Order status event" +
                            " published to Kafka:" +
                            " orderId={}, status={}",
                    orderId, status);
        } catch (Exception e) {
            log.error("Failed to publish" +
                            " Kafka event: {}",
                    e.getMessage());
        }
    }
}
