package com.balu.food_delivery_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationHelper {

    @Async
    public void sendCartUpdateNotification(String customerEmail, String itemName, int quantity) {

        // This runs in background thread
        // Main response already sent to user
        log.info("[BACKGROUND THREAD] Cart " + "notification: customer={}, " + "item={}, quantity={}",
                customerEmail, itemName, quantity);
        // Future: push notification to mobile app
    }
}
