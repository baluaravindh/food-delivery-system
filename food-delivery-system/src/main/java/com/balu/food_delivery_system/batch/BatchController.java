package com.balu.food_delivery_system.batch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch", description = "Batch job trigger APIs")
public class BatchController {

    private final OrderSummaryJobLauncher orderSummaryJobLauncher;

    // POST /api/batch/run-order-summary
    // Access: ADMIN only (batch jobs shouldn't be triggered by customers)
    // Request: none
    // Response: 200 + success message
    // Note: throws Exception (JobLauncher can throw various Spring Batch exceptions)

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Run Order Summary Batch Job")
    @PostMapping("/run-order-summary")
    public ResponseEntity<String> runOrderSummary() throws Exception {
        orderSummaryJobLauncher.runJob();
        return ResponseEntity.ok("Order summary batch job triggered successfully");
    }
}
