package com.balu.food_delivery_system.batch;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;

import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class OrderSummaryItemReader {

    @Bean
    public JdbcCursorItemReader<OrderSummaryDTO> reader(DataSource dataSource) {

        //   Step 1: Create JdbcCursorItemReader<OrderSummaryDTO>
        JdbcCursorItemReader<OrderSummaryDTO> itemReader = new JdbcCursorItemReader<>();

        //   Step 2: Set the DataSource
        itemReader.setDataSource(dataSource);

        //   Step 3: Set the SQL query
        //           (SELECT restaurant_name, COUNT, SUM FROM orders
        //            JOIN restaurants WHERE payment_status = PAID
        //            GROUP BY restaurant_name)
        itemReader.setSql("SELECT r.restaurant_name, " +
                "COUNT(o.id) as total_orders, " +
                "SUM(o.total_amount) as total_revenue " +
                "FROM orders o " +
                "JOIN restaurants r ON o.restaurant_id = r.id " +
                "WHERE o.payment_status = 'PAID' " +
                "GROUP BY r.restaurant_name");

        //   Step 4: Set RowMapper
        //           map rs.getString("restaurant_name") → restaurantName
        //           map rs.getLong("total_orders") → totalOrders
        //           map rs.getBigDecimal("total_revenue") → totalRevenue
        //           map LocalDate.now().toString() → reportDate
        itemReader.setRowMapper((rs, rowNum) -> OrderSummaryDTO.builder()
                .restaurantName(rs.getString("restaurant_name"))
                .totalOrders(rs.getLong("total_orders"))
                .totalRevenue(rs.getBigDecimal("total_revenue"))
                .reportDate(LocalDate.now().toString())
                .build());

        //   Step 5: Return the reader
        return new JdbcCursorItemReaderBuilder<OrderSummaryDTO>()
                .name("orderSummaryReader")
                .dataSource(dataSource)
                .sql("SELECT r.restaurant_name, " +
                        "COUNT(o.id) as total_orders, " +
                        "SUM(o.total_amount) as total_revenue " +
                        "FROM orders o " +
                        "JOIN restaurants r ON o.restaurant_id = r.id " +
                        "WHERE o.payment_status = 'PAID' " +
                        "GROUP BY r.restaurant_name")
                .rowMapper(((rs, rowNum) -> OrderSummaryDTO.builder()
                        .restaurantName(rs.getString("restaurant_name"))
                        .totalOrders(rs.getLong("total_orders"))
                        .totalRevenue(rs.getBigDecimal("total_revenue"))
                        .reportDate(LocalDate.now().toString())
                        .build()))
                .build();
    }
}
