package com.balu.food_delivery_system.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSummaryDTO {

    private String restaurantName;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private String reportDate;
}
