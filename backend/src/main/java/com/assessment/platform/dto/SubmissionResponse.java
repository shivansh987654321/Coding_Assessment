package com.assessment.platform.dto;

import com.assessment.platform.entity.SubmissionStatus;
import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long problemId,
        String problemTitle,
        String language,
        String code,
        SubmissionStatus status,
        Double runtime,
        Integer memory,
        Instant submittedAt,
        String errorOutput,
        ComplexityAnalysis complexityAnalysis
) {
}
