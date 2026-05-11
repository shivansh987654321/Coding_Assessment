package com.assessment.platform.repository;

import com.assessment.platform.entity.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findTop10ByUserClerkUserIdOrderBySubmittedAtDesc(String clerkUserId);
    List<Submission> findByUserClerkUserIdOrderBySubmittedAtDesc(String clerkUserId);
    boolean existsByUserClerkUserIdAndProblemIdAndStatus(String clerkUserId, Long problemId, com.assessment.platform.entity.SubmissionStatus status);
}
