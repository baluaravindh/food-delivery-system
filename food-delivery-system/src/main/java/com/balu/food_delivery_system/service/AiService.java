package com.balu.food_delivery_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiService {

    // fields: ChatClient (injected via ChatClient.Builder)
    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // METHOD: getFoodRecommendation(String query)
    // WHO: CUSTOMER via controller
    // WHAT to do:
    // WHAT to return: String
    public String getFoodRecommendation(String query) {

        //   Step 1: Build a prompt — combine system context + user query
        //           system message: "You are a food recommendation assistant
        //            for an Indian food delivery app. Suggest menu items
        //            based on the user's preference."
        //   Step 2: Call ChatClient to get AI response
        String response = chatClient.prompt()
                .system("You are a food recommendation assistant for an Indian food delivery app. " +
                        "Suggest menu items based on the user's preference.")
                .user(query)
                .call()
                .content();

        //   Step 3: Return the response as String
        log.info("AI recommendation generated for query: {}", query);
        return response;
    }
}
