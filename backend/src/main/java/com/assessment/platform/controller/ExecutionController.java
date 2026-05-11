package com.assessment.platform.controller;

import com.assessment.platform.dto.ExecutionRequest;
import com.assessment.platform.dto.ExecutionResponse;
import com.assessment.platform.dto.SubmissionResponse;
import com.assessment.platform.service.ExecutionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExecutionController {
    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/run")
    public ExecutionResponse run(@RequestBody ExecutionRequest request) {
        return executionService.run(request);
    }

    @PostMapping("/submit")
    public SubmissionResponse submit(@RequestBody ExecutionRequest request) {
        return executionService.submit(request);
    }
}
