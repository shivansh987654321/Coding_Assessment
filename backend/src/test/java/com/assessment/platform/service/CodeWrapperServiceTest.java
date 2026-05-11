package com.assessment.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.assessment.platform.entity.Difficulty;
import com.assessment.platform.entity.Problem;
import org.junit.jupiter.api.Test;

class CodeWrapperServiceTest {

    private final CodeWrapperService codeWrapperService = new CodeWrapperService();

    @Test
    void wrapsTwoSumJavaSolutions() {
        Problem problem = problem("Two Sum");

        String wrapped = codeWrapperService.wrap(problem, "java", "class Solution {}\n");

        assertTrue(wrapped.contains("import java.util.*;"));
        assertTrue(wrapped.contains("class Solution {}"));
        assertTrue(wrapped.contains("public class Main"));
        assertTrue(wrapped.contains("new Solution().twoSum(nums, target)"));
    }

    @Test
    void wrapsTwoSumPythonSolutionsCaseInsensitively() {
        Problem problem = problem("Two Sum");

        String wrapped = codeWrapperService.wrap(problem, "PyThOn", "class Solution:\n    pass\n");

        assertTrue(wrapped.contains("from typing import List"));
        assertTrue(wrapped.contains("class Solution:\n    pass"));
        assertTrue(wrapped.contains("result = Solution().twoSum(nums, target)"));
    }

    @Test
    void wrapsValidPalindromeCppSolutions() {
        Problem problem = problem("Valid Palindrome");

        String wrapped = codeWrapperService.wrap(problem, "cpp", "class Solution {}\n");

        assertTrue(wrapped.contains("#include <bits/stdc++.h>"));
        assertTrue(wrapped.contains("class Solution {}"));
        assertTrue(wrapped.contains("cout << (sol.isPalindrome(s) ? \"true\" : \"false\")"));
    }

    @Test
    void returnsOriginalCodeForUnsupportedProblems() {
        Problem problem = problem("Merge Intervals");
        String userCode = "class Solution {}\n";

        assertEquals(userCode, codeWrapperService.wrap(problem, null, userCode));
    }

    private Problem problem(String title) {
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setDescription("desc");
        problem.setDifficulty(Difficulty.EASY);
        problem.setConstraintsText("constraints");
        problem.setExamples("examples");
        problem.setStarterCode("starter");
        return problem;
    }
}