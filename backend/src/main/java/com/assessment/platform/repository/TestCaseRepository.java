package com.assessment.platform.repository;

import com.assessment.platform.entity.TestCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemId(Long problemId);
    List<TestCase> findByProblemIdAndHidden(Long problemId, boolean hidden);
    @Modifying
    @Transactional
    void deleteByProblemId(Long problemId);
}
