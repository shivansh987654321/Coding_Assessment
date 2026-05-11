package com.assessment.platform.controller;

import com.assessment.platform.dto.ProblemRequest;
import com.assessment.platform.dto.ProblemResponse;
import com.assessment.platform.service.ProblemService;
import com.assessment.platform.service.ProblemSeedService;
import com.assessment.platform.entity.Difficulty;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/problems")
public class ProblemController {
    private final ProblemService problemService;
    private final ProblemSeedService problemSeedService;

    public ProblemController(ProblemService problemService, ProblemSeedService problemSeedService) {
        this.problemService = problemService;
        this.problemSeedService = problemSeedService;
    }

    @GetMapping
    public List<ProblemResponse> all() {
        return problemService.findAll();
    }

    @GetMapping("/{id}")
    public ProblemResponse byId(@PathVariable Long id) {
        return problemService.findById(id);
    }

    @PostMapping
    public ProblemResponse create(@RequestBody ProblemRequest request) {
        return problemService.create(request);
    }

    @GetMapping("/random")
    public ProblemResponse random(String difficulty) {
        Difficulty d = Difficulty.EASY;
        try {
            if (difficulty != null && !difficulty.isBlank()) d = Difficulty.valueOf(difficulty.toUpperCase());
        } catch (Exception ignored) {
        }
        return problemSeedService.randomByDifficulty(d);
    }
}
