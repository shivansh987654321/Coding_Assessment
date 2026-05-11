package com.assessment.platform.service;

import com.assessment.platform.dto.DailyActivity;
import com.assessment.platform.dto.DashboardResponse;
import com.assessment.platform.dto.DifficultyBreakdown;
import com.assessment.platform.dto.SubmissionResponse;
import com.assessment.platform.entity.Difficulty;
import com.assessment.platform.entity.Submission;
import com.assessment.platform.entity.SubmissionStatus;
import com.assessment.platform.repository.ProblemRepository;
import com.assessment.platform.repository.SubmissionRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutionService executionService;

    public DashboardService(ProblemRepository problemRepository, SubmissionRepository submissionRepository, ExecutionService executionService) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.executionService = executionService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse forUser(String clerkUserId) {
        List<Submission> submissions = submissionRepository.findByUserClerkUserIdOrderBySubmittedAtDesc(clerkUserId);

        long accepted = submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
        long solved = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();
        double acceptanceRate = submissions.isEmpty() ? 0 : (accepted * 100.0) / submissions.size();

        List<SubmissionResponse> recent = submissions.stream()
                .limit(10)
                .map(executionService::toSubmissionResponse)
                .toList();

        List<DailyActivity> dailyActivity = buildDailyActivity(submissions);
        DifficultyBreakdown breakdown = buildDifficultyBreakdown(submissions);

        return new DashboardResponse(problemRepository.count(), solved, submissions.size(),
                acceptanceRate, recent, dailyActivity, breakdown);
    }

    private List<DailyActivity> buildDailyActivity(List<Submission> submissions) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        Map<String, long[]> byDay = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            byDay.put(today.minusDays(i).format(fmt), new long[]{0, 0});
        }

        for (Submission s : submissions) {
            String day = s.getSubmittedAt().atZone(zone).toLocalDate().format(fmt);
            long[] counts = byDay.get(day);
            if (counts != null) {
                counts[1]++;
                if (s.getStatus() == SubmissionStatus.ACCEPTED) counts[0]++;
            }
        }

        List<DailyActivity> result = new ArrayList<>();
        for (Map.Entry<String, long[]> e : byDay.entrySet()) {
            result.add(new DailyActivity(e.getKey(), e.getValue()[0], e.getValue()[1]));
        }
        return result;
    }

    private DifficultyBreakdown buildDifficultyBreakdown(List<Submission> submissions) {
        long easy = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED
                        && s.getProblem().getDifficulty() == Difficulty.EASY)
                .map(s -> s.getProblem().getId()).distinct().count();
        long medium = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED
                        && s.getProblem().getDifficulty() == Difficulty.MEDIUM)
                .map(s -> s.getProblem().getId()).distinct().count();
        long hard = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED
                        && s.getProblem().getDifficulty() == Difficulty.HARD)
                .map(s -> s.getProblem().getId()).distinct().count();
        return new DifficultyBreakdown(easy, medium, hard);
    }
}
