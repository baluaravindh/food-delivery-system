package com.balu.food_delivery_system.controller;

import com.balu.food_delivery_system.dto.AiRecommendationRequestDTO;
import com.balu.food_delivery_system.dto.AiRecommendationResponseDTO;
import com.balu.food_delivery_system.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI", description = "AI food recommendation APIs")
public class AiController {

    private final AiService aiService;

    // POST /api/ai/recommendations
    // Access: CUSTOMER only
    // Request: @RequestBody AiRecommendationRequestDTO
    // Response: 200 + AiRecommendationResponseDTO
    @Operation(summary = "Get Food Recommendation")
    @PostMapping("/recommendations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AiRecommendationResponseDTO> getFoodRecommendation(
            @RequestBody AiRecommendationRequestDTO dto) {
        String response = aiService.getFoodRecommendation(dto.getQuery());
        return ResponseEntity.ok(new AiRecommendationResponseDTO(response));
    }
}
