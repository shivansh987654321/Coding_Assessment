package com.assessment.platform.dto;

import java.util.List;

public record DashboardResponse(
        long totalProblems,
        long solvedProblems,
        long totalSubmissions,
        double acceptanceRate,
        List<SubmissionResponse> recentSubmissions,
        List<DailyActivity> dailyActivity,
        DifficultyBreakdown solvedByDifficulty
) {
}
