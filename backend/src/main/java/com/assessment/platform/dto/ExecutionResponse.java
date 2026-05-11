package com.assessment.platform.dto;

import com.assessment.platform.entity.SubmissionStatus;

public record ExecutionResponse(
        SubmissionStatus status,
        String stdout,
        String stderr,
        String compileOutput,
        Double runtime,
        Integer memory,
        String message
) {
}
