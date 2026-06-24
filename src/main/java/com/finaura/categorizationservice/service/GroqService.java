package com.finaura.categorizationservice.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GroqService {

    private final WebClient webClient;

    public GroqService() {
        String apiKey = System.getenv("GROQ_API_KEY") != null ? System.getenv("GROQ_API_KEY") : System.getProperty("GROQ_API_KEY");
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String categorizeExpense(String item, String merchant, String notes) {
        System.out.println("\n========== GROQ REQUEST ==========");
        System.out.println("Item     : " + item);
        System.out.println("Merchant : " + merchant);
        System.out.println("Notes    : " + notes);
        System.out.println("==================================");

        String prompt = 
        """
            Categorize the expense into EXACTLY ONE category.

            Allowed Categories:
            Housing & Utilities
            Food & Essentials
            Transport & Travel
            Health & Wellness
            Personal & Lifestyle
            Financial & Others

            Expense:
            Item: %s
            Merchant: %s
            Notes: %s

            Rules:
            - Return only the category name
            - No explanation
            - No punctuation
            - No extra words
        """.formatted(item, merchant, notes);

        Map<String, Object> requestBody = Map.of("model", "llama-3.3-70b-versatile", "messages", List.of(Map.of("role", "user", "content", prompt)), "temperature", 0);
        
        Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        if (response == null) throw new RuntimeException("Groq returned null response");

        System.out.println("\n========== RAW RESPONSE ==========");
        System.out.println(response);
        System.out.println("==================================");

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if(choices == null) throw new RuntimeException("No choices returned by Groq");

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if(message == null) throw new RuntimeException("No message found in Groq Response");

        String category = message.get("content").toString().trim();
        System.out.println("\n========== CATEGORY ==========");
        System.out.println(category);
        System.out.println("==============================");

        return category;
    }
}