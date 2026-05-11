package com.assessment.platform.service;

import com.assessment.platform.dto.ComplexityAnalysis;
import com.assessment.platform.dto.ExecutionRequest;
import com.assessment.platform.dto.ExecutionResponse;
import com.assessment.platform.dto.Judge0SubmissionRequest;
import com.assessment.platform.dto.Judge0SubmissionResponse;
import com.assessment.platform.dto.SubmissionResponse;
import com.assessment.platform.dto.UserSyncRequest;
import com.assessment.platform.entity.Problem;
import com.assessment.platform.entity.Submission;
import com.assessment.platform.entity.SubmissionStatus;
import com.assessment.platform.entity.TestCase;
import com.assessment.platform.entity.User;
import com.assessment.platform.repository.SubmissionRepository;
import com.assessment.platform.repository.TestCaseRepository;
import com.assessment.platform.util.LanguageMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionService {
    private final Judge0Client judge0Client;
    private final ProblemService problemService;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final CodeWrapperService codeWrapperService;
    private final GroqClient groqClient;

    public ExecutionService(
            Judge0Client judge0Client,
            ProblemService problemService,
            TestCaseRepository testCaseRepository,
            SubmissionRepository submissionRepository,
            UserService userService,
            CodeWrapperService codeWrapperService,
            GroqClient groqClient
    ) {
        this.judge0Client = judge0Client;
        this.problemService = problemService;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.codeWrapperService = codeWrapperService;
        this.groqClient = groqClient;
    }

    public ExecutionResponse run(ExecutionRequest request) {
        Problem problem = problemService.getProblem(request.problemId());
        String input = request.input();
        if (input == null || input.isBlank()) {
            input = testCaseRepository.findByProblemIdAndHidden(problem.getId(), false)
                    .stream()
                    .findFirst()
                    .map(TestCase::getInput)
                    .orElse("");
        }
        Judge0SubmissionResponse result = judge0Client.execute(toJudgeRequest(request, problem, input));
        return toExecutionResponse(result, null);
    }

    @Transactional
    public SubmissionResponse submit(ExecutionRequest request) {
        User user = userService.sync(new UserSyncRequest(request.clerkUserId(), request.name(), request.email()));
        Problem problem = problemService.getProblem(request.problemId());
        List<TestCase> testCases = testCaseRepository.findByProblemIdAndHidden(problem.getId(), true);
        if (testCases.isEmpty()) {
            testCases = testCaseRepository.findByProblemId(problem.getId());
        }

        SubmissionStatus finalStatus = SubmissionStatus.ACCEPTED;
        Double maxRuntime = 0.0;
        Integer maxMemory = 0;
        String errorOutput = null;

        for (TestCase testCase : testCases) {
            Judge0SubmissionResponse result = judge0Client.execute(toJudgeRequest(request, problem, testCase.getInput()));
            SubmissionStatus status = toStatus(result);
            maxRuntime = Math.max(maxRuntime, parseRuntime(result.time()));
            maxMemory = Math.max(maxMemory, result.memory() == null ? 0 : result.memory());

            if (status != SubmissionStatus.ACCEPTED) {
                finalStatus = status;
                errorOutput = buildErrorOutput(result);
                break;
            }

            if (!normalize(result.stdout()).equals(normalize(testCase.getExpectedOutput()))) {
                finalStatus = SubmissionStatus.WRONG_ANSWER;
                errorOutput = buildWrongAnswerOutput(result, testCase);
                break;
            }
        }

        ComplexityAnalysis complexity = null;
        if (finalStatus == SubmissionStatus.ACCEPTED) {
            complexity = groqClient.analyzeComplexity(request.code(), request.language());
        }

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(request.language());
        submission.setCode(request.code());
        submission.setStatus(finalStatus);
        submission.setRuntime(maxRuntime);
        submission.setMemory(maxMemory);
        SubmissionResponse saved = toSubmissionResponse(submissionRepository.save(submission));
        return new SubmissionResponse(saved.id(), saved.problemId(), saved.problemTitle(),
                saved.language(), saved.code(), saved.status(), saved.runtime(), saved.memory(),
                saved.submittedAt(), errorOutput, complexity);
    }

    public SubmissionResponse toSubmissionResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getProblem().getTitle(),
                submission.getLanguage(),
                submission.getCode(),
                submission.getStatus(),
                submission.getRuntime(),
                submission.getMemory(),
                submission.getSubmittedAt(),
                null,
                null
        );
    }

    private String buildErrorOutput(Judge0SubmissionResponse result) {
        StringBuilder sb = new StringBuilder();
        if (result.compileOutput() != null && !result.compileOutput().isBlank()) {
            sb.append(result.compileOutput().strip());
        }
        if (result.stderr() != null && !result.stderr().isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append(result.stderr().strip());
        }
        if (result.message() != null && !result.message().isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append(result.message().strip());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private String buildWrongAnswerOutput(Judge0SubmissionResponse result, TestCase testCase) {
        String actual = result.stdout() == null ? "(no output)" : result.stdout().strip();
        String expected = testCase.getExpectedOutput() == null ? "" : testCase.getExpectedOutput().strip();
        return "Expected:\n" + expected + "\n\nGot:\n" + actual;
    }

    private Judge0SubmissionRequest toJudgeRequest(ExecutionRequest request, Problem problem, String stdin) {
        return new Judge0SubmissionRequest(
                codeWrapperService.wrap(problem, request.language(), request.code()),
                LanguageMapper.toJudge0Id(request.language()),
                stdin == null ? "" : stdin
        );
    }

    private ExecutionResponse toExecutionResponse(Judge0SubmissionResponse result, String message) {
        return new ExecutionResponse(
                toStatus(result),
                result.stdout(),
                result.stderr(),
                result.compileOutput(),
                parseRuntime(result.time()),
                result.memory(),
                message == null ? result.message() : message
        );
    }

    private SubmissionStatus toStatus(Judge0SubmissionResponse result) {
        Integer statusId = result == null || result.status() == null ? null : result.status().id();
        if (Objects.equals(statusId, 3)) {
            return SubmissionStatus.ACCEPTED;
        }
        if (Objects.equals(statusId, 4)) {
            return SubmissionStatus.WRONG_ANSWER;
        }
        if (Objects.equals(statusId, 5)) {
            return SubmissionStatus.TIME_LIMIT_EXCEEDED;
        }
        if (Objects.equals(statusId, 6)) {
            return SubmissionStatus.COMPILATION_ERROR;
        }
        if (statusId != null && statusId >= 7 && statusId <= 12) {
            return SubmissionStatus.RUNTIME_ERROR;
        }
        return SubmissionStatus.INTERNAL_ERROR;
    }

    private Double parseRuntime(String time) {
        try {
            return time == null ? 0.0 : Double.parseDouble(time);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\R", "\n");
    }
}
