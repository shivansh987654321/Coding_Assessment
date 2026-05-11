package com.assessment.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Judge0SubmissionRequest(
        @JsonProperty("source_code") String sourceCode,
        @JsonProperty("language_id") Integer languageId,
        String stdin
) {
}
