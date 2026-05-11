package com.assessment.platform.config;

import com.assessment.platform.entity.Difficulty;
import com.assessment.platform.entity.Problem;
import com.assessment.platform.entity.TestCase;
import com.assessment.platform.repository.ProblemRepository;
import com.assessment.platform.repository.TestCaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    CommandLineRunner seedProblems(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) {
                Problem twoSum = problemRepository.findAll().stream()
                        .filter(problem -> "Two Sum".equalsIgnoreCase(problem.getTitle()))
                        .findFirst()
                        .orElseGet(Problem::new);
                twoSum.setTitle("Two Sum");
                twoSum.setDifficulty(Difficulty.EASY);
                twoSum.setTags(List.of("Array", "Hash Table"));
                twoSum.setDescription("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.");
                twoSum.setConstraintsText("2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\nExactly one valid answer exists.");
                twoSum.setExamples("Input:\nnums = [2,7,11,15], target = 9\nOutput:\n[0,1]\nExplanation:\nBecause nums[0] + nums[1] == 9, return [0, 1].");
                twoSum.setStarterCode("""
                        class Solution {
                            public int[] twoSum(int[] nums, int target) {

                            }
                        }
                        """);
                problemRepository.save(twoSum);
                testCaseRepository.deleteByProblemId(twoSum.getId());
                SeedTestCases.twoSumCases().forEach(testCase ->
                        addCase(testCaseRepository, twoSum, testCase.input(), testCase.expectedOutput(), testCase.hidden()));

                Problem palindrome = problemRepository.findAll().stream()
                        .filter(problem -> "Valid Palindrome".equalsIgnoreCase(problem.getTitle()))
                        .findFirst()
                        .orElseGet(Problem::new);
                palindrome.setTitle("Valid Palindrome");
                palindrome.setDifficulty(Difficulty.EASY);
                palindrome.setTags(List.of("String", "Two Pointers"));
                palindrome.setDescription("Given a string, determine if it is a palindrome after converting uppercase letters into lowercase letters and removing non-alphanumeric characters.");
                palindrome.setConstraintsText("1 <= s.length <= 2 * 10^5");
                palindrome.setExamples("Input:\ns = \"A man, a plan, a canal: Panama\"\nOutput:\ntrue\nExplanation:\n\"amanaplanacanalpanama\" is a palindrome.");
                palindrome.setStarterCode("""
                        class Solution {
                            public boolean isPalindrome(String s) {

                            }
                        }
                        """);
                problemRepository.save(palindrome);
                testCaseRepository.deleteByProblemId(palindrome.getId());
                SeedTestCases.validPalindromeCases().forEach(testCase ->
                        addCase(testCaseRepository, palindrome, testCase.input(), testCase.expectedOutput(), testCase.hidden()));

                Set<String> existingTitles = problemRepository.findAll().stream()
                        .map(p -> p.getTitle().toLowerCase())
                        .collect(Collectors.toSet());

                seedFromProblemsJson(problemRepository, testCaseRepository, existingTitles);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void seedFromProblemsJson(ProblemRepository problemRepo, TestCaseRepository testCaseRepo, Set<String> existingTitles) {
        try (InputStream is = DataSeeder.class.getResourceAsStream("/problems.json")) {
            if (is == null) return;
            Map<String, Object> root = mapper.readValue(is, new TypeReference<>() {});
            List<Map<String, Object>> problems = (List<Map<String, Object>>) root.get("problems");
            if (problems == null) return;

            for (Map<String, Object> entry : problems) {
                String title = str(entry.get("title"));
                if (title.isBlank() || existingTitles.contains(title.toLowerCase())) continue;

                Difficulty difficulty = parseDifficulty(str(entry.get("difficulty")));
                String description = buildDescription(entry);
                String constraintsText = buildConstraints(entry);
                String examples = buildExamples((List<Map<String, String>>) entry.getOrDefault("examples", List.of()));
                List<String> tags = buildTags(entry);

                Problem problem = new Problem();
                problem.setTitle(title);
                problem.setDifficulty(difficulty);
                problem.setDescription(description);
                problem.setConstraintsText(constraintsText);
                problem.setExamples(examples);
                problem.setStarterCode(javaStarter(str(entry.get("id")), title));
                problem.setTags(tags);
                problemRepo.save(problem);
                existingTitles.add(title.toLowerCase());

                List<Map<String, Object>> testCases =
                        (List<Map<String, Object>>) entry.getOrDefault("testCases", List.of());
                for (Map<String, Object> tc : testCases) {
                    String input = str(tc.get("input"));
                    String expected = str(tc.get("expectedOutput"));
                    boolean hidden = Boolean.TRUE.equals(tc.get("isHidden"));
                    if (!input.isBlank() && !expected.isBlank()) {
                        addCase(testCaseRepo, problem, input + "\n", expected + "\n", hidden);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String buildDescription(Map<String, Object> entry) {
        StringBuilder sb = new StringBuilder();
        String desc = str(entry.get("description"));
        if (!desc.isBlank()) sb.append(desc);
        String inputFmt = str(entry.get("inputFormat"));
        if (!inputFmt.isBlank()) sb.append("\n\nInput Format:\n").append(inputFmt);
        String outputFmt = str(entry.get("outputFormat"));
        if (!outputFmt.isBlank()) sb.append("\n\nOutput Format:\n").append(outputFmt);
        return sb.toString().isBlank() ? "Implement: " + str(entry.get("title")) : sb.toString();
    }

    private String buildConstraints(Map<String, Object> entry) {
        Object raw = entry.get("constraints");
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
        return raw == null ? "" : String.valueOf(raw);
    }

    private String buildExamples(List<Map<String, String>> examples) {
        if (examples == null || examples.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> ex : examples) {
            sb.append("Input: ").append(ex.getOrDefault("input", "")).append("\n");
            sb.append("Output: ").append(ex.getOrDefault("output", "")).append("\n");
            String explanation = ex.get("explanation");
            if (explanation != null && !explanation.isBlank()) {
                sb.append("Explanation: ").append(explanation).append("\n");
            }
            sb.append("---\n");
        }
        return sb.toString();
    }

    private List<String> buildTags(Map<String, Object> entry) {
        List<String> tags = new ArrayList<>();
        String topic = str(entry.get("topic"));
        if (!topic.isBlank()) tags.add(topic);
        Object rawTags = entry.get("tags");
        if (rawTags instanceof List<?> list) {
            list.stream().map(Object::toString).filter(t -> !t.isBlank()).forEach(tags::add);
        }
        return tags;
    }

    private Difficulty parseDifficulty(String value) {
        return switch (value.toLowerCase()) {
            case "medium" -> Difficulty.MEDIUM;
            case "hard" -> Difficulty.HARD;
            default -> Difficulty.EASY;
        };
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private String javaStarter(String id, String title) {
        return switch (id) {
            case "set-matrix-zeroes"            -> "class Solution {\n    public void setZeroes(int[][] matrix) {\n        \n    }\n}";
            case "pascals-triangle"             -> "class Solution {\n    public List<List<Integer>> generate(int numRows) {\n        \n    }\n}";
            case "next-permutation"             -> "class Solution {\n    public void nextPermutation(int[] nums) {\n        \n    }\n}";
            case "maximum-subarray"             -> "class Solution {\n    public int maxSubArray(int[] nums) {\n        \n    }\n}";
            case "sort-colors"                  -> "class Solution {\n    public void sortColors(int[] nums) {\n        \n    }\n}";
            case "best-time-to-buy-sell-stock"  -> "class Solution {\n    public int maxProfit(int[] prices) {\n        \n    }\n}";
            case "rotate-image"                 -> "class Solution {\n    public void rotate(int[][] matrix) {\n        \n    }\n}";
            case "merge-intervals"              -> "class Solution {\n    public int[][] merge(int[][] intervals) {\n        \n    }\n}";
            case "merge-sorted-array"           -> "class Solution {\n    public void merge(int[] nums1, int m, int[] nums2, int n) {\n        \n    }\n}";
            case "find-duplicate-number"        -> "class Solution {\n    public int findDuplicate(int[] nums) {\n        \n    }\n}";
            case "repeat-and-missing"           -> "class Solution {\n    public int[] findTwoElement(int[] arr) {\n        \n    }\n}";
            case "count-inversions"             -> "class Solution {\n    public long inversionCount(long[] arr, int n) {\n        \n    }\n}";
            case "search-2d-matrix"             -> "class Solution {\n    public boolean searchMatrix(int[][] matrix, int target) {\n        \n    }\n}";
            case "pow-x-n"                      -> "class Solution {\n    public double myPow(double x, int n) {\n        \n    }\n}";
            case "majority-element-n2"          -> "class Solution {\n    public int majorityElement(int[] nums) {\n        \n    }\n}";
            case "majority-element-n3"          -> "class Solution {\n    public List<Integer> majorityElement(int[] nums) {\n        \n    }\n}";
            case "unique-paths"                 -> "class Solution {\n    public int uniquePaths(int m, int n) {\n        \n    }\n}";
            case "reverse-pairs"                -> "class Solution {\n    public int reversePairs(int[] nums) {\n        \n    }\n}";
            case "four-sum"                     -> "class Solution {\n    public List<List<Integer>> fourSum(int[] nums, int target) {\n        \n    }\n}";
            case "longest-consecutive-sequence" -> "class Solution {\n    public int longestConsecutive(int[] nums) {\n        \n    }\n}";
            case "largest-subarray-zero-sum"    -> "class Solution {\n    public int maxLen(int[] arr, int n) {\n        \n    }\n}";
            case "subarray-given-xor"           -> "class Solution {\n    public int subarraysWithXorK(int[] a, int k) {\n        \n    }\n}";
            case "longest-substring-no-repeat"  -> "class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        \n    }\n}";
            case "reverse-linked-list"          -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode reverseList(ListNode head) {\n        \n    }\n}";
            case "middle-of-linked-list"        -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode middleNode(ListNode head) {\n        \n    }\n}";
            case "merge-two-sorted-lists"       -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {\n        \n    }\n}";
            case "remove-nth-from-end"          -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode removeNthFromEnd(ListNode head, int n) {\n        \n    }\n}";
            case "add-two-numbers"              -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {\n        \n    }\n}";
            case "delete-node-no-head"          -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public void deleteNode(ListNode node) {\n        \n    }\n}";
            case "intersection-two-lists"       -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {\n        \n    }\n}";
            case "linked-list-cycle"            -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public boolean hasCycle(ListNode head) {\n        \n    }\n}";
            case "reverse-k-group"              -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode reverseKGroup(ListNode head, int k) {\n        \n    }\n}";
            case "palindrome-linked-list"       -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public boolean isPalindrome(ListNode head) {\n        \n    }\n}";
            case "linked-list-cycle-ii"         -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode detectCycle(ListNode head) {\n        \n    }\n}";
            case "flatten-linked-list"          -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public Node flatten(Node head) {\n        \n    }\n}";
            case "rotate-list"                  -> "// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\nclass Solution {\n    public ListNode rotateRight(ListNode head, int k) {\n        \n    }\n}";
            case "three-sum"                    -> "class Solution {\n    public List<List<Integer>> threeSum(int[] nums) {\n        \n    }\n}";
            case "trapping-rain-water"          -> "class Solution {\n    public int trap(int[] height) {\n        \n    }\n}";
            case "remove-duplicates-sorted"     -> "class Solution {\n    public int removeDuplicates(int[] nums) {\n        \n    }\n}";
            case "max-consecutive-ones"         -> "class Solution {\n    public int findMaxConsecutiveOnes(int[] nums) {\n        \n    }\n}";
            case "n-meetings-one-room"          -> "class Solution {\n    public int maxMeetings(int[] start, int[] end, int n) {\n        \n    }\n}";
            case "min-platforms"               -> "class Solution {\n    public int findPlatform(int[] arr, int[] dep, int n) {\n        \n    }\n}";
            case "job-sequencing"              -> "class Solution {\n    public int[] jobSequencing(int[] id, int[] deadline, int[] profit) {\n        \n    }\n}";
            case "fractional-knapsack"         -> "class Solution {\n    public double fractionalKnapsack(int W, int[] weight, int[] value) {\n        \n    }\n}";
            case "min-coins"                   -> "class Solution {\n    public int minCoins(int[] coins, int V) {\n        \n    }\n}";
            case "activity-selection"          -> "class Solution {\n    public int activitySelection(int[] start, int[] end, int n) {\n        \n    }\n}";
            case "subset-sums"                 -> "class Solution {\n    public List<Integer> subsetSums(List<Integer> arr, int n) {\n        \n    }\n}";
            case "combination-sum-1"           -> "class Solution {\n    public List<List<Integer>> combinationSum(int[] candidates, int target) {\n        \n    }\n}";
            case "combination-sum-2"           -> "class Solution {\n    public List<List<Integer>> combinationSum2(int[] candidates, int target) {\n        \n    }\n}";
            case "palindrome-partitioning"     -> "class Solution {\n    public List<List<String>> partition(String s) {\n        \n    }\n}";
            case "permutation-sequence"        -> "class Solution {\n    public String getPermutation(int n, int k) {\n        \n    }\n}";
            case "permutations"                -> "class Solution {\n    public List<List<Integer>> permute(int[] nums) {\n        \n    }\n}";
            case "n-queens"                    -> "class Solution {\n    public List<List<String>> solveNQueens(int n) {\n        \n    }\n}";
            case "sudoku-solver"               -> "class Solution {\n    public void solveSudoku(char[][] board) {\n        \n    }\n}";
            case "m-coloring"                  -> "class Solution {\n    public boolean graphColoring(boolean[][] graph, int m, int n) {\n        \n    }\n}";
            case "rat-in-maze"                 -> "class Solution {\n    public List<String> findPath(int[][] m, int n) {\n        \n    }\n}";
            case "nth-root"                    -> "class Solution {\n    public int NthRoot(int n, int m) {\n        \n    }\n}";
            case "single-element-sorted"       -> "class Solution {\n    public int singleNonDuplicate(int[] nums) {\n        \n    }\n}";
            case "search-rotated-sorted"       -> "class Solution {\n    public int search(int[] nums, int target) {\n        \n    }\n}";
            case "median-two-sorted"           -> "class Solution {\n    public double findMedianSortedArrays(int[] nums1, int[] nums2) {\n        \n    }\n}";
            case "allocate-pages"              -> "class Solution {\n    public int findPages(int[] arr, int n, int m) {\n        \n    }\n}";
            case "aggressive-cows"             -> "class Solution {\n    public int aggressiveCows(int[] stalls, int k) {\n        \n    }\n}";
            case "kth-largest-element"         -> "class Solution {\n    public int findKthLargest(int[] nums, int k) {\n        \n    }\n}";
            case "merge-k-sorted-arrays"       -> "class Solution {\n    public int[][] mergeKArrays(int[][] arr, int K) {\n        \n    }\n}";
            case "top-k-frequent"              -> "class Solution {\n    public int[] topKFrequent(int[] nums, int k) {\n        \n    }\n}";
            case "valid-parentheses"           -> "class Solution {\n    public boolean isValid(String s) {\n        \n    }\n}";
            case "next-greater-element"        -> "class Solution {\n    public int[] nextGreaterElement(int[] nums1, int[] nums2) {\n        \n    }\n}";
            case "lru-cache"                   -> "class LRUCache {\n    public LRUCache(int capacity) {\n        \n    }\n    public int get(int key) {\n        \n    }\n    public void put(int key, int value) {\n        \n    }\n}";
            case "largest-rectangle-histogram" -> "class Solution {\n    public int largestRectangleArea(int[] heights) {\n        \n    }\n}";
            case "sliding-window-max"          -> "class Solution {\n    public int[] maxSlidingWindow(int[] nums, int k) {\n        \n    }\n}";
            case "min-stack"                   -> "class MinStack {\n    public MinStack() {\n        \n    }\n    public void push(int val) {\n        \n    }\n    public void pop() {\n        \n    }\n    public int top() {\n        \n    }\n    public int getMin() {\n        \n    }\n}";
            case "rotting-oranges"             -> "class Solution {\n    public int orangesRotting(int[][] grid) {\n        \n    }\n}";
            case "stock-span"                  -> "class Solution {\n    public int[] calculateSpan(int[] price, int n) {\n        \n    }\n}";
            case "reverse-words"               -> "class Solution {\n    public String reverseWords(String s) {\n        \n    }\n}";
            case "longest-palindrome-substring"-> "class Solution {\n    public String longestPalindrome(String s) {\n        \n    }\n}";
            case "roman-integer"               -> "class Solution {\n    public int romanToInt(String s) {\n        \n    }\n}";
            case "atoi"                        -> "class Solution {\n    public int myAtoi(String s) {\n        \n    }\n}";
            case "longest-common-prefix"       -> "class Solution {\n    public String longestCommonPrefix(String[] strs) {\n        \n    }\n}";
            case "kmp-strstr"                  -> "class Solution {\n    public int strStr(String haystack, String needle) {\n        \n    }\n}";
            case "valid-anagram"               -> "class Solution {\n    public boolean isAnagram(String s, String t) {\n        \n    }\n}";
            case "count-and-say"               -> "class Solution {\n    public String countAndSay(int n) {\n        \n    }\n}";
            case "compare-version"             -> "class Solution {\n    public int compareVersion(String version1, String version2) {\n        \n    }\n}";
            default                            -> "class Solution {\n    public void solve() {\n        // implement your solution here\n    }\n}";
        };
    }

    private void addCase(TestCaseRepository repository, Problem problem, String input, String expected, boolean hidden) {
        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        testCase.setInput(input);
        testCase.setExpectedOutput(expected);
        testCase.setHidden(hidden);
        repository.save(testCase);
    }
}
