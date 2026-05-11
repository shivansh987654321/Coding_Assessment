package com.assessment.platform.controller;

import com.assessment.platform.dto.SubmissionResponse;
import com.assessment.platform.repository.SubmissionRepository;
import com.assessment.platform.service.ExecutionService;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {
    private final SubmissionRepository submissionRepository;
    private final ExecutionService executionService;

    public SubmissionController(SubmissionRepository submissionRepository, ExecutionService executionService) {
        this.submissionRepository = submissionRepository;
        this.executionService = executionService;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<SubmissionResponse> all(@RequestParam String clerkUserId) {
        return submissionRepository.findByUserClerkUserIdOrderBySubmittedAtDesc(clerkUserId)
                .stream()
                .map(executionService::toSubmissionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public SubmissionResponse byId(@PathVariable Long id) {
        return submissionRepository.findById(id)
                .map(executionService::toSubmissionResponse)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + id));
    }
}
