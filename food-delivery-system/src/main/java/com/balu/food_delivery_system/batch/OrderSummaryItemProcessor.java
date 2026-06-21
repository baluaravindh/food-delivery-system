package com.balu.food_delivery_system.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderSummaryItemProcessor implements ItemProcessor<OrderSummaryDTO, OrderSummaryDTO> {

    @Override
    public OrderSummaryDTO process(OrderSummaryDTO item) throws Exception {

        //   Step 1: log.info "[BATCH] Processing restaurant: {} orders: {} revenue: {}"
        //           use item.getRestaurantName(), item.getTotalOrders(), item.getTotalRevenue()
        log.info("[BATCH] Processing restaurant: {} orders: {} revenue: {}",
                item.getRestaurantName(), item.getTotalOrders(), item.getTotalRevenue());

        //   Step 2: return item
        return item;
    }
}
