package com.assessment.platform.dto;

import com.assessment.platform.entity.Difficulty;
import java.time.Instant;
import java.util.List;

public record ProblemResponse(
        Long id,
        String title,
        String description,
        Difficulty difficulty,
        String constraintsText,
        String examples,
        String starterCode,
        List<String> tags,
        Instant createdAt
) {
}
