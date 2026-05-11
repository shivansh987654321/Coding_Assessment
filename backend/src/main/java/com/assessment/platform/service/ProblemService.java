package com.assessment.platform.service;

import com.assessment.platform.dto.ProblemRequest;
import com.assessment.platform.dto.ProblemResponse;
import com.assessment.platform.entity.Problem;
import com.assessment.platform.repository.ProblemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProblemResponse> findAll() {
        return problemRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProblemResponse findById(Long id) {
        return toResponse(getProblem(id));
    }

    @Transactional
    public ProblemResponse create(ProblemRequest request) {
        Problem problem = new Problem();
        problem.setTitle(request.title());
        problem.setDescription(request.description());
        problem.setDifficulty(request.difficulty());
        problem.setConstraintsText(request.constraintsText());
        problem.setExamples(request.examples());
        problem.setStarterCode(request.starterCode());
        problem.setTags(request.tags() == null ? List.of() : request.tags());
        return toResponse(problemRepository.save(problem));
    }

    public Problem getProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
    }

    private ProblemResponse toResponse(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getConstraintsText(),
                problem.getExamples(),
                problem.getStarterCode(),
                problem.getTags(),
                problem.getCreatedAt()
        );
    }
}
