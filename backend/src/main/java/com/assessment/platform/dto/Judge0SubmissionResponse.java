package com.assessment.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Judge0SubmissionResponse(
        String stdout,
        String stderr,
        @JsonProperty("compile_output") String compileOutput,
        String message,
        Judge0Status status,
        String time,
        Integer memory
) {
    public record Judge0Status(Integer id, String description) {
    }
}
