package com.balu.food_delivery_system.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;

@Component
@Slf4j
public class OrderSummaryItemWriter implements ItemWriter<OrderSummaryDTO> {

    @Override
    public void write(Chunk<? extends OrderSummaryDTO> chunk) throws Exception {

        //   Step 1: Build file path
        //           e.g. "logs/order-summary-" + LocalDate.now() + ".csv"
        String filepath = "logs/order-summary-" + LocalDate.now() + ".csv";
        boolean isFileNew = new File(filepath).length() == 0;

        //   Step 2: Open FileWriter in append mode (true)
        //           wrap in BufferedWriter for efficiency
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {

            //   Step 3: Write CSV header line
            //           "Restaurant Name,Total Orders,Total Revenue,Report Date"
            //           (only write header if file is new/empty)
            if (isFileNew) {
                writer.write("Restaurant Name,Total Orders,Total Revenue,Report Date");
                writer.newLine();
            }

            //   Step 4: Loop through chunk.getItems()
            //           for each item write a CSV line:
            //           item.getRestaurantName() + "," + item.getTotalOrders()
            //           + "," + item.getTotalRevenue() + "," + item.getReportDate()
            for (OrderSummaryDTO order : chunk.getItems()) {
                writer.write(order.getRestaurantName() + "," + order.getTotalOrders() + ","
                        + order.getTotalRevenue() + "," + order.getReportDate());
            }
        }

        //   Step 5: log.info "[BATCH] Written {} records to {}"
        //           chunk.getItems().size(), filePath
        log.info("[BATCH] Written {} records to {}", chunk.getItems().size(), filepath);
    }
}
