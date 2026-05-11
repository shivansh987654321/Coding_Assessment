package com.assessment.platform.service;

import com.assessment.platform.dto.ComplexityAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqClient {

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GroqClient(@Value("${groq.api-key:}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
    }

    public ComplexityAnalysis analyzeComplexity(String code, String language) {
        if (apiKey == null || apiKey.isBlank() || code == null || code.isBlank()) {
            return null;
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", "llama3-8b-8192");
            body.put("temperature", 0.1);
            body.put("max_tokens", 150);

            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", buildPrompt(code, language));

            String response = restClient.post()
                    .uri("/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            int start = content.indexOf('{');
            int end = content.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                return objectMapper.readValue(content.substring(start, end), ComplexityAnalysis.class);
            }
        } catch (Exception e) {
            // fail silently — complexity analysis is non-critical
        }
        return null;
    }

    private String buildPrompt(String code, String language) {
        return "Analyze this " + language + " code for time and space complexity.\n"
                + "Reply ONLY with a JSON object — no explanation, no markdown:\n"
                + "{\"timeComplexity\":\"O(?)\",\"spaceComplexity\":\"O(?)\",\"suggestion\":\"one concise tip\"}\n\n"
                + "Code:\n" + code;
    }
}
