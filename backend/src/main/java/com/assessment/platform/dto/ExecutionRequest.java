package com.assessment.platform.dto;

public record ExecutionRequest(
        String clerkUserId,
        String name,
        String email,
        Long problemId,
        String language,
        String code,
        String input
) {
}
