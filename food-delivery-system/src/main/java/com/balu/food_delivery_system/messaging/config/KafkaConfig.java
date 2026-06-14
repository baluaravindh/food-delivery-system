package com.balu.food_delivery_system.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${fds.kafka.topic.order-status}")
    private String orderStatusTopic;

    @Bean
    public NewTopic orderStatusTopic() {
        return TopicBuilder
                .name(orderStatusTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
