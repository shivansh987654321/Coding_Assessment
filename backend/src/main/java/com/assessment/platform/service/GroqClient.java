package com.assessment.platform.service;

import com.assessment.platform.dto.ComplexityAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GroqClient(@Value("${groq.api-key:}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public ComplexityAnalysis analyzeComplexity(String code, String language) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GROQ_API_KEY is not set — skipping complexity analysis");
            return null;
        }
        try {
            String body = buildRequestBody(code, language);
            log.info("Calling Groq API for complexity analysis...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Groq response status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {}", response.body());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            log.info("Groq content: {}", content);

            int start = content.indexOf('{');
            int end = content.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                ComplexityAnalysis result = objectMapper.readValue(content.substring(start, end), ComplexityAnalysis.class);
                log.info("Complexity analysis: {}", result);
                return result;
            }
            log.warn("Could not parse JSON from Groq response: {}", content);
        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
        }
        return null;
    }

    private String buildRequestBody(String code, String language) throws Exception {
        String prompt = "Analyze this " + language + " code for time and space complexity.\n"
                + "Reply ONLY with a JSON object — no explanation, no markdown:\n"
                + "{\"timeComplexity\":\"O(?)\",\"spaceComplexity\":\"O(?)\",\"suggestion\":\"one concise tip\"}\n\n"
                + "Code:\n" + code;

        return objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("model", "llama3-70b-8192")
                        .put("temperature", 0.1)
                        .put("max_tokens", 150)
                        .set("messages", objectMapper.createArrayNode()
                                .add(objectMapper.createObjectNode()
                                        .put("role", "user")
                                        .put("content", prompt)))
        );
    }
}
