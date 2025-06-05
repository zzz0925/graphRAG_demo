// ModelService.java (模拟AI模型调用)
package com.chat.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class ModelServiceImpl {

    private final Random random = new Random();
    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    // It's good practice to make RestTemplate a bean or configure it properly
    // For simplicity, instantiating directly here.
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String userMessage) {
        return callAiApi(userMessage);
    }

    private String callAiApi(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // Typically for authentication
        //headers.set("Accept","text/event-stream");
        // --- IMPORTANT: Customize this part based on your LLM API's request format ---
        // Example for a generic chat completion API:
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-plus"); // Or "claude-3-opus-20240229", etc.
        requestBody.put("messages", Collections.singletonList(
                Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("max_tokens", 5000); // Adjust as needed
        requestBody.put("temperature", 0.7); // Adjust for creativity vs. focus
        //requestBody.put("stream",true);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // --- IMPORTANT: Customize the response type based on your LLM API's response format ---
            // This example assumes a structure like:
            // { "choices": [ { "message": { "content": "..." } } ], ... }
            ResponseEntity<Map> response = restTemplate.postForEntity(aiApiUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Extracting the content from the response
                // You'll need to inspect the actual API response structure
                // and adjust this parsing logic accordingly.
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("choices")) {
                    // Assuming 'choices' is a list
                    Object choices = responseBody.get("choices");
                    if (choices instanceof java.util.List && !((java.util.List) choices).isEmpty()) {
                        Map<String, Object> firstChoice = (Map<String, Object>) ((java.util.List) choices).get(0);
                        if (firstChoice.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                            if (message.containsKey("content")) {
                                return (String) message.get("content");
                            }
                        }
                    }
                }
                return "AI API returned an unexpected response format.";
            } else {
                return "Error calling AI API: " + response.getStatusCode() + " - " + response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Exception calling AI API: " + e.getMessage());
            return "Failed to get response from AI API: " + e.getMessage();
        }
    }
}

    /*public String generateResponse(String userMessage) {
        // 这里应该调用实际的AI模型API
        // 现在只是返回模拟响应
        log.info("Received user message: " + userMessage);
        try {
            // 模拟API调用延迟
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String baseResponse = responses[random.nextInt(responses.length)];
        return baseResponse + "\n\n针对您的问题：\"" + userMessage + "\"，我建议您可以从多个角度来思考这个问题。";
    }*/