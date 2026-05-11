package com.assessment.platform.service;

import com.assessment.platform.dto.Judge0SubmissionRequest;
import com.assessment.platform.dto.Judge0SubmissionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Judge0Client {
    private final RestClient restClient;
    private final String apiKey;
    private final String apiHost;

    public Judge0Client(
            @Value("${judge0.base-url}") String baseUrl,
            @Value("${judge0.api-key}") String apiKey,
            @Value("${judge0.api-host}") String apiHost
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.apiHost = apiHost;
    }

    public Judge0SubmissionResponse execute(Judge0SubmissionRequest request) {
        return restClient.post()
                .uri("/submissions?base64_encoded=false&wait=true")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::addJudgeHeaders)
                .body(request)
                .retrieve()
                .body(Judge0SubmissionResponse.class);
    }

    private void addJudgeHeaders(HttpHeaders headers) {
        if (apiKey != null && !apiKey.isBlank()) {
            headers.add("X-RapidAPI-Key", apiKey);
            headers.add("X-RapidAPI-Host", apiHost);
        }
    }
}
