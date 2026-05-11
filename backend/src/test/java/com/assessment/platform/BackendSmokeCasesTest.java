package com.assessment.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.assessment.platform.dto.ProblemRequest;
import com.assessment.platform.dto.ProblemResponse;
import com.assessment.platform.entity.Difficulty;
import com.assessment.platform.entity.Problem;
import com.assessment.platform.repository.ProblemRepository;
import com.assessment.platform.service.CodeWrapperService;
import com.assessment.platform.service.ProblemService;
import com.assessment.platform.util.LanguageMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackendSmokeCasesTest {

    private final CodeWrapperService codeWrapperService = new CodeWrapperService();

    @Mock
    private ProblemRepository problemRepository;

    @ParameterizedTest(name = "Language mapper case {index}")
    @MethodSource("languageCases")
    void languageMapperCases(String language, Integer expectedId) {
        assertEquals(expectedId, LanguageMapper.toJudge0Id(language));
    }

    Stream<Arguments> languageCases() {
        return Stream.of(
                Arguments.of(null, 62),
                Arguments.of("java", 62),
                Arguments.of("JAVA", 62),
                Arguments.of("javascript", 63),
                Arguments.of("PyThOn", 71),
                Arguments.of("cpp", 54),
                Arguments.of("unknown", 62)
        );
    }

    @ParameterizedTest(name = "Code wrapper case {index}")
    @MethodSource("wrapperCases")
    void codeWrapperCases(String title, String language, String userCode, String expected, boolean exactMatch) {
        Problem problem = problemEntity(title, List.of());

        String wrapped = codeWrapperService.wrap(problem, language, userCode);

        if (exactMatch) {
            assertEquals(expected, wrapped);
        } else {
            assertTrue(wrapped.contains(expected));
        }
    }

    Stream<Arguments> wrapperCases() {
        return Stream.of(
                Arguments.of("Two Sum", "java", "class Solution {}\n", "import java.util.*;", false),
                Arguments.of("Two Sum", "java", "class Solution {}\n", "public class Main", false),
                Arguments.of("Two Sum", "python", "class Solution:\n    pass\n", "from typing import List", false),
                Arguments.of("Two Sum", "python", "class Solution:\n    pass\n", "result = Solution().twoSum(nums, target)", false),
                Arguments.of("Valid Palindrome", "java", "class Solution {}\n", "new Solution().isPalindrome(s)", false),
                Arguments.of("Valid Palindrome", "cpp", "class Solution {}\n", "cout << (sol.isPalindrome(s) ? \"true\" : \"false\")", false),
                Arguments.of("Merge Intervals", null, "class Solution {}\n", "class Solution {}\n", true)
        );
    }

    @ParameterizedTest(name = "Problem service case {index}")
    @MethodSource("problemCases")
    void problemServiceCases(ProblemCase problemCase) {
        ProblemService problemService = new ProblemService(problemRepository);

        switch (problemCase.scenario()) {
            case FIND_ALL_EMPTY -> {
                when(problemRepository.findAll()).thenReturn(List.of());
                assertEquals(List.of(), problemService.findAll());
            }
            case FIND_ALL_ONE -> {
                when(problemRepository.findAll()).thenReturn(List.of(problemEntity(1L, "Two Sum", List.of("array"))));
                List<ProblemResponse> responses = problemService.findAll();
                assertEquals(1, responses.size());
                assertEquals("Two Sum", responses.get(0).title());
                assertEquals(List.of("array"), responses.get(0).tags());
            }
            case FIND_ALL_TWO -> {
                when(problemRepository.findAll()).thenReturn(List.of(
                    problemEntity(1L, "Two Sum", List.of("array", "hashmap")),
                    problemEntity(2L, "Valid Palindrome", List.of("string"))
                ));
                List<ProblemResponse> responses = problemService.findAll();
                assertEquals(2, responses.size());
                assertEquals("Two Sum", responses.get(0).title());
                assertEquals("Valid Palindrome", responses.get(1).title());
            }
            case FIND_BY_ID -> {
                when(problemRepository.findById(problemCase.id())).thenReturn(Optional.of(problemCase.problem()));
                ProblemResponse response = problemService.findById(problemCase.id());
                assertEquals(problemCase.id(), response.id());
                assertEquals(problemCase.expectedTitle(), response.title());
                assertEquals(problemCase.expectedTags(), response.tags());
            }
            case CREATE_NULL_TAGS -> {
                when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
                    Problem saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 99L);
                    ReflectionTestUtils.setField(saved, "createdAt", Instant.parse("2024-01-04T10:00:00Z"));
                    return saved;
                });
                ProblemResponse response = problemService.create(problemCase.request());
                assertEquals(99L, response.id());
                assertEquals(List.of(), response.tags());
            }
            case CREATE_TAGS -> {
                when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
                    Problem saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 100L);
                    ReflectionTestUtils.setField(saved, "createdAt", Instant.parse("2024-01-05T10:00:00Z"));
                    return saved;
                });
                ProblemResponse response = problemService.create(problemCase.request());
                assertEquals(100L, response.id());
                assertEquals(problemCase.expectedTags(), response.tags());
                assertEquals(problemCase.expectedTitle(), response.title());
            }
            case GET_MISSING -> {
                when(problemRepository.findById(problemCase.id())).thenReturn(Optional.empty());
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> problemService.getProblem(problemCase.id()));
                assertEquals("Problem not found: " + problemCase.id(), exception.getMessage());
            }
        }
    }

    Stream<Arguments> problemCases() {
        return Stream.of(
                Arguments.of(ProblemCase.findAllEmpty()),
                Arguments.of(ProblemCase.findAllOne()),
                Arguments.of(ProblemCase.findAllTwo()),
                Arguments.of(ProblemCase.findById()),
                Arguments.of(ProblemCase.createNullTags()),
                Arguments.of(ProblemCase.createTags()),
                Arguments.of(ProblemCase.getMissing())
        );
    }

    private Problem problemEntity(Long id, String title, List<String> tags) {
        Problem problem = new Problem();
        ReflectionTestUtils.setField(problem, "id", id);
        ReflectionTestUtils.setField(problem, "title", title);
        ReflectionTestUtils.setField(problem, "description", "description");
        ReflectionTestUtils.setField(problem, "difficulty", Difficulty.EASY);
        ReflectionTestUtils.setField(problem, "constraintsText", "constraints");
        ReflectionTestUtils.setField(problem, "examples", "examples");
        ReflectionTestUtils.setField(problem, "starterCode", "starter");
        ReflectionTestUtils.setField(problem, "tags", tags);
        ReflectionTestUtils.setField(problem, "createdAt", Instant.parse("2024-01-01T00:00:00Z"));
        return problem;
    }

    private Problem problemEntity(String title, List<String> tags) {
        Problem problem = new Problem();
        ReflectionTestUtils.setField(problem, "title", title);
        ReflectionTestUtils.setField(problem, "description", "description");
        ReflectionTestUtils.setField(problem, "difficulty", Difficulty.EASY);
        ReflectionTestUtils.setField(problem, "constraintsText", "constraints");
        ReflectionTestUtils.setField(problem, "examples", "examples");
        ReflectionTestUtils.setField(problem, "starterCode", "starter");
        ReflectionTestUtils.setField(problem, "tags", tags);
        ReflectionTestUtils.setField(problem, "createdAt", Instant.parse("2024-01-01T00:00:00Z"));
        return problem;
    }

    private ProblemRequest request(String title, List<String> tags) {
        return new ProblemRequest(
                title,
                "description",
                Difficulty.EASY,
                "constraints",
                "examples",
                "starter",
                tags
        );
    }

    private enum Scenario {
        FIND_ALL_EMPTY,
        FIND_ALL_ONE,
        FIND_ALL_TWO,
        FIND_BY_ID,
        CREATE_NULL_TAGS,
        CREATE_TAGS,
        GET_MISSING
    }

    private record ProblemCase(
            Scenario scenario,
            Long id,
            Problem problem,
            ProblemRequest request,
            List<String> expectedTags,
            String expectedTitle
    ) {
        static ProblemCase findAllEmpty() {
            return new ProblemCase(Scenario.FIND_ALL_EMPTY, null, null, null, null, null);
        }

        static ProblemCase findAllOne() {
            return new ProblemCase(Scenario.FIND_ALL_ONE, null, null, null, null, null);
        }

        static ProblemCase findAllTwo() {
            return new ProblemCase(Scenario.FIND_ALL_TWO, null, null, null, null, null);
        }

        static ProblemCase findById() {
            return new ProblemCase(
                    Scenario.FIND_BY_ID,
                    7L,
                    problem(7L, "Two Sum", List.of("array")),
                    null,
                    List.of("array"),
                    "Two Sum"
            );
        }

        static ProblemCase createNullTags() {
            return new ProblemCase(
                    Scenario.CREATE_NULL_TAGS,
                    null,
                    null,
                    new ProblemRequest("Two Sum", "description", Difficulty.EASY, "constraints", "examples", "starter", null),
                    List.of(),
                    "Two Sum"
            );
        }

        static ProblemCase createTags() {
            return new ProblemCase(
                    Scenario.CREATE_TAGS,
                    null,
                    null,
                    new ProblemRequest("Valid Palindrome", "description", Difficulty.EASY, "constraints", "examples", "starter", List.of("string", "two-pointers")),
                    List.of("string", "two-pointers"),
                    "Valid Palindrome"
            );
        }

        static ProblemCase getMissing() {
            return new ProblemCase(Scenario.GET_MISSING, 42L, null, null, null, null);
        }

        private static Problem problem(Long id, String title, List<String> tags) {
            Problem problem = new Problem();
            ReflectionTestUtils.setField(problem, "id", id);
            ReflectionTestUtils.setField(problem, "title", title);
            ReflectionTestUtils.setField(problem, "description", "description");
            ReflectionTestUtils.setField(problem, "difficulty", Difficulty.EASY);
            ReflectionTestUtils.setField(problem, "constraintsText", "constraints");
            ReflectionTestUtils.setField(problem, "examples", "examples");
            ReflectionTestUtils.setField(problem, "starterCode", "starter");
            ReflectionTestUtils.setField(problem, "tags", tags);
            ReflectionTestUtils.setField(problem, "createdAt", Instant.parse("2024-01-01T00:00:00Z"));
            return problem;
        }
    }
}