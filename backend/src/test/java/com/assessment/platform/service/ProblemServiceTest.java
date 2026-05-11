package com.assessment.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assessment.platform.dto.ProblemRequest;
import com.assessment.platform.dto.ProblemResponse;
import com.assessment.platform.entity.Difficulty;
import com.assessment.platform.entity.Problem;
import com.assessment.platform.repository.ProblemRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Test
    void findAllMapsProblemsToResponses() {
        ProblemService problemService = new ProblemService(problemRepository);
        when(problemRepository.findAll()).thenReturn(List.of(
                problem(1L, "Two Sum", Instant.parse("2024-01-01T10:00:00Z"), List.of("array", "hashmap")),
                problem(2L, "Valid Palindrome", Instant.parse("2024-01-02T10:00:00Z"), List.of("string"))
        ));

        List<ProblemResponse> responses = problemService.findAll();

        assertEquals(2, responses.size());
        assertEquals("Two Sum", responses.get(0).title());
        assertEquals(List.of("array", "hashmap"), responses.get(0).tags());
        assertEquals("Valid Palindrome", responses.get(1).title());
        assertEquals(List.of("string"), responses.get(1).tags());
    }

    @Test
    void findByIdReturnsMappedProblem() {
        ProblemService problemService = new ProblemService(problemRepository);
        Problem problem = problem(7L, "Two Sum", Instant.parse("2024-01-03T10:00:00Z"), List.of("array"));
        when(problemRepository.findById(7L)).thenReturn(Optional.of(problem));

        ProblemResponse response = problemService.findById(7L);

        assertEquals(7L, response.id());
        assertEquals("Two Sum", response.title());
        assertEquals(Difficulty.EASY, response.difficulty());
        assertEquals(List.of("array"), response.tags());
        assertEquals(problem.getCreatedAt(), response.createdAt());
    }

    @Test
    void createDefaultsNullTagsToEmptyList() {
        ProblemService problemService = new ProblemService(problemRepository);
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 99L);
            ReflectionTestUtils.setField(saved, "createdAt", Instant.parse("2024-01-04T10:00:00Z"));
            return saved;
        });

        ProblemRequest request = new ProblemRequest(
                "Two Sum",
                "Find two numbers",
                Difficulty.EASY,
                "constraints",
                "examples",
                "starter",
                null
        );

        ProblemResponse response = problemService.create(request);

        ArgumentCaptor<Problem> captor = ArgumentCaptor.forClass(Problem.class);
        verify(problemRepository).save(captor.capture());
        assertEquals(List.of(), captor.getValue().getTags());
        assertEquals(99L, response.id());
        assertEquals("Two Sum", response.title());
        assertEquals(List.of(), response.tags());
    }

    @Test
    void getProblemThrowsWhenMissing() {
        ProblemService problemService = new ProblemService(problemRepository);
        when(problemRepository.findById(42L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> problemService.getProblem(42L));

        assertEquals("Problem not found: 42", exception.getMessage());
    }

    private Problem problem(Long id, String title, Instant createdAt, List<String> tags) {
        Problem problem = new Problem();
        ReflectionTestUtils.setField(problem, "id", id);
        ReflectionTestUtils.setField(problem, "title", title);
        ReflectionTestUtils.setField(problem, "description", "description");
        ReflectionTestUtils.setField(problem, "difficulty", Difficulty.EASY);
        ReflectionTestUtils.setField(problem, "constraintsText", "constraints");
        ReflectionTestUtils.setField(problem, "examples", "examples");
        ReflectionTestUtils.setField(problem, "starterCode", "starter");
        ReflectionTestUtils.setField(problem, "tags", tags);
        ReflectionTestUtils.setField(problem, "createdAt", createdAt);
        return problem;
    }
}