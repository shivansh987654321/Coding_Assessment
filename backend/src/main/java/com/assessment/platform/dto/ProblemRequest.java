package com.assessment.platform.dto;

import com.assessment.platform.entity.Difficulty;
import java.util.List;

public record ProblemRequest(
        String title,
        String description,
        Difficulty difficulty,
        String constraintsText,
        String examples,
        String starterCode,
        List<String> tags
) {
}
