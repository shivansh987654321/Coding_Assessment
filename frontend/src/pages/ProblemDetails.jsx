import Editor from '@monaco-editor/react'
import { CheckCircle2, FileText, Play, Send, TerminalSquare } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useUser } from '@clerk/clerk-react'
import Badge from '../components/Badge'
import Loading from '../components/Loading'
import { getBackendUserPayload } from '../hooks/useBackendUser'
import { api } from '../services/api'
import { difficultyClasses, statusClasses, statusLabel } from '../utils/status'

const languages = [
  { value: 'java',   label: 'Java'   },
  { value: 'cpp',    label: 'C++'    },
  { value: 'python', label: 'Python' },
]

const LL_JAVA  = `// class ListNode { int val; ListNode next; ListNode(int x){val=x;} }\n`
const LL_CPP   = `// struct ListNode { int val; ListNode *next; ListNode(int x):val(x),next(nullptr){} };\n`
const LL_PY    = `# class ListNode:\n#     def __init__(self,val=0,next=None): self.val=val; self.next=next\n`

const LANGUAGE_STARTERS = {
  // ── Already-wrapped problems ──────────────────────────────────────────────
  'Two Sum': {
    java:   `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def twoSum(self, nums, target):\n        pass`,
  },
  'Valid Palindrome': {
    java:   `class Solution {\n    public boolean isPalindrome(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    bool isPalindrome(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def isPalindrome(self, s: str) -> bool:\n        pass`,
  },

  // ── Arrays ────────────────────────────────────────────────────────────────
  'Set Matrix Zeroes': {
    java:   `class Solution {\n    public void setZeroes(int[][] matrix) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void setZeroes(vector<vector<int>>& matrix) {\n        \n    }\n};`,
    python: `class Solution:\n    def setZeroes(self, matrix: List[List[int]]) -> None:\n        pass`,
  },
  "Pascal's Triangle": {
    java:   `class Solution {\n    public List<List<Integer>> generate(int numRows) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> generate(int numRows) {\n        \n    }\n};`,
    python: `class Solution:\n    def generate(self, numRows: int) -> List[List[int]]:\n        pass`,
  },
  'Next Permutation': {
    java:   `class Solution {\n    public void nextPermutation(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void nextPermutation(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def nextPermutation(self, nums: List[int]) -> None:\n        pass`,
  },
  "Maximum Subarray Sum (Kadane's Algorithm)": {
    java:   `class Solution {\n    public int maxSubArray(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int maxSubArray(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def maxSubArray(self, nums: List[int]) -> int:\n        pass`,
  },
  'Sort Colors (Dutch National Flag)': {
    java:   `class Solution {\n    public void sortColors(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void sortColors(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def sortColors(self, nums: List[int]) -> None:\n        pass`,
  },
  'Best Time to Buy and Sell Stock': {
    java:   `class Solution {\n    public int maxProfit(int[] prices) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int maxProfit(vector<int>& prices) {\n        \n    }\n};`,
    python: `class Solution:\n    def maxProfit(self, prices: List[int]) -> int:\n        pass`,
  },
  'Rotate Matrix by 90 Degrees': {
    java:   `class Solution {\n    public void rotate(int[][] matrix) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void rotate(vector<vector<int>>& matrix) {\n        \n    }\n};`,
    python: `class Solution:\n    def rotate(self, matrix: List[List[int]]) -> None:\n        pass`,
  },
  'Merge Overlapping Intervals': {
    java:   `class Solution {\n    public int[][] merge(int[][] intervals) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> merge(vector<vector<int>>& intervals) {\n        \n    }\n};`,
    python: `class Solution:\n    def merge(self, intervals: List[List[int]]) -> List[List[int]]:\n        pass`,
  },
  'Merge Two Sorted Arrays In-Place': {
    java:   `class Solution {\n    public void merge(int[] nums1, int m, int[] nums2, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void merge(vector<int>& nums1, int m, vector<int>& nums2, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def merge(self, nums1: List[int], m: int, nums2: List[int], n: int) -> None:\n        pass`,
  },
  'Find the Duplicate Number': {
    java:   `class Solution {\n    public int findDuplicate(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int findDuplicate(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def findDuplicate(self, nums: List[int]) -> int:\n        pass`,
  },
  'Repeat and Missing Number': {
    java:   `class Solution {\n    public int[] findTwoElement(int[] arr) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> findTwoElement(vector<int>& arr) {\n        \n    }\n};`,
    python: `class Solution:\n    def findTwoElement(self, arr: List[int]) -> List[int]:\n        pass`,
  },
  'Count Inversions in Array': {
    java:   `class Solution {\n    public long inversionCount(long[] arr, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    long long inversionCount(vector<long long>& arr, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def inversionCount(self, arr: List[int], n: int) -> int:\n        pass`,
  },
  'Search in a 2D Matrix': {
    java:   `class Solution {\n    public boolean searchMatrix(int[][] matrix, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    bool searchMatrix(vector<vector<int>>& matrix, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def searchMatrix(self, matrix: List[List[int]], target: int) -> bool:\n        pass`,
  },
  'Pow(x, n)': {
    java:   `class Solution {\n    public double myPow(double x, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    double myPow(double x, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def myPow(self, x: float, n: int) -> float:\n        pass`,
  },
  'Majority Element (more than n/2 times)': {
    java:   `class Solution {\n    public int majorityElement(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int majorityElement(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def majorityElement(self, nums: List[int]) -> int:\n        pass`,
  },
  'Majority Element II (more than n/3 times)': {
    java:   `class Solution {\n    public List<Integer> majorityElement(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> majorityElement(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def majorityElement(self, nums: List[int]) -> List[int]:\n        pass`,
  },
  'Grid Unique Paths': {
    java:   `class Solution {\n    public int uniquePaths(int m, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int uniquePaths(int m, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def uniquePaths(self, m: int, n: int) -> int:\n        pass`,
  },
  'Reverse Pairs': {
    java:   `class Solution {\n    public int reversePairs(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int reversePairs(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def reversePairs(self, nums: List[int]) -> int:\n        pass`,
  },

  // ── Arrays / Hashing ──────────────────────────────────────────────────────
  '4 Sum': {
    java:   `class Solution {\n    public List<List<Integer>> fourSum(int[] nums, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> fourSum(vector<int>& nums, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def fourSum(self, nums: List[int], target: int) -> List[List[int]]:\n        pass`,
  },
  'Longest Consecutive Sequence': {
    java:   `class Solution {\n    public int longestConsecutive(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int longestConsecutive(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def longestConsecutive(self, nums: List[int]) -> int:\n        pass`,
  },
  'Largest Subarray with Zero Sum': {
    java:   `class Solution {\n    public int maxLen(int[] arr, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int maxLen(vector<int>& arr, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def maxLen(self, arr: List[int], n: int) -> int:\n        pass`,
  },
  'Count Subarrays with Given XOR': {
    java:   `class Solution {\n    public int subarraysWithXorK(int[] a, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int subarraysWithXorK(vector<int>& a, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def subarraysWithXorK(self, a: List[int], k: int) -> int:\n        pass`,
  },
  'Longest Substring Without Repeating Characters': {
    java:   `class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int lengthOfLongestSubstring(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def lengthOfLongestSubstring(self, s: str) -> int:\n        pass`,
  },

  // ── Linked List ───────────────────────────────────────────────────────────
  'Reverse a Linked List': {
    java:   LL_JAVA + `class Solution {\n    public ListNode reverseList(ListNode head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* reverseList(ListNode* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def reverseList(self, head):\n        pass`,
  },
  'Middle of the Linked List': {
    java:   LL_JAVA + `class Solution {\n    public ListNode middleNode(ListNode head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* middleNode(ListNode* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def middleNode(self, head):\n        pass`,
  },
  'Merge Two Sorted Linked Lists': {
    java:   LL_JAVA + `class Solution {\n    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* mergeTwoLists(ListNode* l1, ListNode* l2) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def mergeTwoLists(self, l1, l2):\n        pass`,
  },
  'Remove Nth Node From End of List': {
    java:   LL_JAVA + `class Solution {\n    public ListNode removeNthFromEnd(ListNode head, int n) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* removeNthFromEnd(ListNode* head, int n) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def removeNthFromEnd(self, head, n: int):\n        pass`,
  },
  'Add Two Numbers (Linked List)': {
    java:   LL_JAVA + `class Solution {\n    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* addTwoNumbers(ListNode* l1, ListNode* l2) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def addTwoNumbers(self, l1, l2):\n        pass`,
  },
  'Delete Node Without Head Pointer': {
    java:   LL_JAVA + `class Solution {\n    public void deleteNode(ListNode node) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    void deleteNode(ListNode* node) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def deleteNode(self, node):\n        pass`,
  },
  'Intersection of Two Linked Lists': {
    java:   LL_JAVA + `class Solution {\n    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* getIntersectionNode(ListNode* headA, ListNode* headB) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def getIntersectionNode(self, headA, headB):\n        pass`,
  },
  'Detect Cycle in Linked List': {
    java:   LL_JAVA + `class Solution {\n    public boolean hasCycle(ListNode head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    bool hasCycle(ListNode* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def hasCycle(self, head) -> bool:\n        pass`,
  },
  'Reverse Linked List in Groups of K': {
    java:   LL_JAVA + `class Solution {\n    public ListNode reverseKGroup(ListNode head, int k) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* reverseKGroup(ListNode* head, int k) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def reverseKGroup(self, head, k: int):\n        pass`,
  },
  'Check if Linked List is Palindrome': {
    java:   LL_JAVA + `class Solution {\n    public boolean isPalindrome(ListNode head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    bool isPalindrome(ListNode* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def isPalindrome(self, head) -> bool:\n        pass`,
  },
  'Find Starting Point of Cycle': {
    java:   LL_JAVA + `class Solution {\n    public ListNode detectCycle(ListNode head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* detectCycle(ListNode* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def detectCycle(self, head):\n        pass`,
  },
  'Flatten a Multilevel Linked List': {
    java:   LL_JAVA + `class Solution {\n    public Node flatten(Node head) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    Node* flatten(Node* head) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def flatten(self, head):\n        pass`,
  },
  'Rotate Linked List': {
    java:   LL_JAVA + `class Solution {\n    public ListNode rotateRight(ListNode head, int k) {\n        \n    }\n}`,
    cpp:    LL_CPP  + `class Solution {\npublic:\n    ListNode* rotateRight(ListNode* head, int k) {\n        \n    }\n};`,
    python: LL_PY   + `class Solution:\n    def rotateRight(self, head, k: int):\n        pass`,
  },

  // ── Two Pointers ──────────────────────────────────────────────────────────
  '3 Sum': {
    java:   `class Solution {\n    public List<List<Integer>> threeSum(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> threeSum(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def threeSum(self, nums: List[int]) -> List[List[int]]:\n        pass`,
  },
  'Trapping Rain Water': {
    java:   `class Solution {\n    public int trap(int[] height) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int trap(vector<int>& height) {\n        \n    }\n};`,
    python: `class Solution:\n    def trap(self, height: List[int]) -> int:\n        pass`,
  },
  'Remove Duplicates from Sorted Array': {
    java:   `class Solution {\n    public int removeDuplicates(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int removeDuplicates(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def removeDuplicates(self, nums: List[int]) -> int:\n        pass`,
  },
  'Max Consecutive Ones': {
    java:   `class Solution {\n    public int findMaxConsecutiveOnes(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int findMaxConsecutiveOnes(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def findMaxConsecutiveOnes(self, nums: List[int]) -> int:\n        pass`,
  },

  // ── Greedy ────────────────────────────────────────────────────────────────
  'N Meetings in One Room': {
    java:   `class Solution {\n    public int maxMeetings(int[] start, int[] end, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int maxMeetings(vector<int>& start, vector<int>& end, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def maxMeetings(self, start: List[int], end: List[int], n: int) -> int:\n        pass`,
  },
  'Minimum Number of Platforms': {
    java:   `class Solution {\n    public int findPlatform(int[] arr, int[] dep, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int findPlatform(vector<int>& arr, vector<int>& dep, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def findPlatform(self, arr: List[int], dep: List[int], n: int) -> int:\n        pass`,
  },
  'Job Sequencing Problem': {
    java:   `class Solution {\n    public int[] jobSequencing(int[] id, int[] deadline, int[] profit) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> jobSequencing(vector<int>& id, vector<int>& deadline, vector<int>& profit) {\n        \n    }\n};`,
    python: `class Solution:\n    def jobSequencing(self, id: List[int], deadline: List[int], profit: List[int]) -> List[int]:\n        pass`,
  },
  'Fractional Knapsack': {
    java:   `class Solution {\n    public double fractionalKnapsack(int W, int[] weight, int[] value) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    double fractionalKnapsack(int W, vector<int>& weight, vector<int>& value) {\n        \n    }\n};`,
    python: `class Solution:\n    def fractionalKnapsack(self, W: int, weight: List[int], value: List[int]) -> float:\n        pass`,
  },
  'Minimum Number of Coins': {
    java:   `class Solution {\n    public int minCoins(int[] coins, int V) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int minCoins(vector<int>& coins, int V) {\n        \n    }\n};`,
    python: `class Solution:\n    def minCoins(self, coins: List[int], V: int) -> int:\n        pass`,
  },
  'Activity Selection': {
    java:   `class Solution {\n    public int activitySelection(int[] start, int[] end, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int activitySelection(vector<int>& start, vector<int>& end, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def activitySelection(self, start: List[int], end: List[int], n: int) -> int:\n        pass`,
  },

  // ── Recursion ─────────────────────────────────────────────────────────────
  'Subset Sums': {
    java:   `class Solution {\n    public List<Integer> subsetSums(List<Integer> arr, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> subsetSums(vector<int>& arr, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def subsetSums(self, arr: List[int], n: int) -> List[int]:\n        pass`,
  },
  'Combination Sum': {
    java:   `class Solution {\n    public List<List<Integer>> combinationSum(int[] candidates, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> combinationSum(vector<int>& candidates, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def combinationSum(self, candidates: List[int], target: int) -> List[List[int]]:\n        pass`,
  },
  'Combination Sum II': {
    java:   `class Solution {\n    public List<List<Integer>> combinationSum2(int[] candidates, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> combinationSum2(vector<int>& candidates, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def combinationSum2(self, candidates: List[int], target: int) -> List[List[int]]:\n        pass`,
  },
  'Palindrome Partitioning': {
    java:   `class Solution {\n    public List<List<String>> partition(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<string>> partition(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def partition(self, s: str) -> List[List[str]]:\n        pass`,
  },
  'K-th Permutation Sequence': {
    java:   `class Solution {\n    public String getPermutation(int n, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    string getPermutation(int n, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def getPermutation(self, n: int, k: int) -> str:\n        pass`,
  },

  // ── Backtracking ──────────────────────────────────────────────────────────
  'Print All Permutations': {
    java:   `class Solution {\n    public List<List<Integer>> permute(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> permute(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def permute(self, nums: List[int]) -> List[List[int]]:\n        pass`,
  },
  'N Queens': {
    java:   `class Solution {\n    public List<List<String>> solveNQueens(int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<string>> solveNQueens(int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def solveNQueens(self, n: int) -> List[List[str]]:\n        pass`,
  },
  'Sudoku Solver': {
    java:   `class Solution {\n    public void solveSudoku(char[][] board) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    void solveSudoku(vector<vector<char>>& board) {\n        \n    }\n};`,
    python: `class Solution:\n    def solveSudoku(self, board: List[List[str]]) -> None:\n        pass`,
  },
  'M Coloring Problem': {
    java:   `class Solution {\n    public boolean graphColoring(boolean[][] graph, int m, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    bool graphColoring(vector<vector<bool>>& graph, int m, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def graphColoring(self, graph: List[List[bool]], m: int, n: int) -> bool:\n        pass`,
  },
  'Rat in a Maze': {
    java:   `class Solution {\n    public List<String> findPath(int[][] m, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<string> findPath(vector<vector<int>>& m, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def findPath(self, m: List[List[int]], n: int) -> List[str]:\n        pass`,
  },

  // ── Binary Search ─────────────────────────────────────────────────────────
  'Nth Root of an Integer': {
    java:   `class Solution {\n    public int NthRoot(int n, int m) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int NthRoot(int n, int m) {\n        \n    }\n};`,
    python: `class Solution:\n    def NthRoot(self, n: int, m: int) -> int:\n        pass`,
  },
  'Single Element in a Sorted Array': {
    java:   `class Solution {\n    public int singleNonDuplicate(int[] nums) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int singleNonDuplicate(vector<int>& nums) {\n        \n    }\n};`,
    python: `class Solution:\n    def singleNonDuplicate(self, nums: List[int]) -> int:\n        pass`,
  },
  'Search in Rotated Sorted Array': {
    java:   `class Solution {\n    public int search(int[] nums, int target) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int search(vector<int>& nums, int target) {\n        \n    }\n};`,
    python: `class Solution:\n    def search(self, nums: List[int], target: int) -> int:\n        pass`,
  },
  'Median of Two Sorted Arrays': {
    java:   `class Solution {\n    public double findMedianSortedArrays(int[] nums1, int[] nums2) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    double findMedianSortedArrays(vector<int>& nums1, vector<int>& nums2) {\n        \n    }\n};`,
    python: `class Solution:\n    def findMedianSortedArrays(self, nums1: List[int], nums2: List[int]) -> float:\n        pass`,
  },
  'Allocate Minimum Number of Pages': {
    java:   `class Solution {\n    public int findPages(int[] arr, int n, int m) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int findPages(vector<int>& arr, int n, int m) {\n        \n    }\n};`,
    python: `class Solution:\n    def findPages(self, arr: List[int], n: int, m: int) -> int:\n        pass`,
  },
  'Aggressive Cows': {
    java:   `class Solution {\n    public int aggressiveCows(int[] stalls, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int aggressiveCows(vector<int>& stalls, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def aggressiveCows(self, stalls: List[int], k: int) -> int:\n        pass`,
  },

  // ── Heap ──────────────────────────────────────────────────────────────────
  'K-th Largest Element in an Array': {
    java:   `class Solution {\n    public int findKthLargest(int[] nums, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int findKthLargest(vector<int>& nums, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def findKthLargest(self, nums: List[int], k: int) -> int:\n        pass`,
  },
  'Merge K Sorted Arrays': {
    java:   `class Solution {\n    public int[][] mergeKArrays(int[][] arr, int K) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<vector<int>> mergeKArrays(vector<vector<int>>& arr, int K) {\n        \n    }\n};`,
    python: `class Solution:\n    def mergeKArrays(self, arr: List[List[int]], K: int) -> List[List[int]]:\n        pass`,
  },
  'Top K Frequent Elements': {
    java:   `class Solution {\n    public int[] topKFrequent(int[] nums, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> topKFrequent(vector<int>& nums, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def topKFrequent(self, nums: List[int], k: int) -> List[int]:\n        pass`,
  },

  // ── Stack / Queue ─────────────────────────────────────────────────────────
  'Check for Balanced Parentheses': {
    java:   `class Solution {\n    public boolean isValid(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    bool isValid(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def isValid(self, s: str) -> bool:\n        pass`,
  },
  'Next Greater Element': {
    java:   `class Solution {\n    public int[] nextGreaterElement(int[] nums1, int[] nums2) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> nextGreaterElement(vector<int>& nums1, vector<int>& nums2) {\n        \n    }\n};`,
    python: `class Solution:\n    def nextGreaterElement(self, nums1: List[int], nums2: List[int]) -> List[int]:\n        pass`,
  },
  'LRU Cache': {
    java:   `class LRUCache {\n    public LRUCache(int capacity) {\n        \n    }\n    public int get(int key) {\n        \n    }\n    public void put(int key, int value) {\n        \n    }\n}`,
    cpp:    `class LRUCache {\npublic:\n    LRUCache(int capacity) {\n        \n    }\n    int get(int key) {\n        \n    }\n    void put(int key, int value) {\n        \n    }\n};`,
    python: `class LRUCache:\n    def __init__(self, capacity: int):\n        pass\n    def get(self, key: int) -> int:\n        pass\n    def put(self, key: int, value: int) -> None:\n        pass`,
  },
  'Largest Rectangle in Histogram': {
    java:   `class Solution {\n    public int largestRectangleArea(int[] heights) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int largestRectangleArea(vector<int>& heights) {\n        \n    }\n};`,
    python: `class Solution:\n    def largestRectangleArea(self, heights: List[int]) -> int:\n        pass`,
  },
  'Sliding Window Maximum': {
    java:   `class Solution {\n    public int[] maxSlidingWindow(int[] nums, int k) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> maxSlidingWindow(vector<int>& nums, int k) {\n        \n    }\n};`,
    python: `class Solution:\n    def maxSlidingWindow(self, nums: List[int], k: int) -> List[int]:\n        pass`,
  },
  'Min Stack': {
    java:   `class MinStack {\n    public MinStack() {\n        \n    }\n    public void push(int val) {\n        \n    }\n    public void pop() {\n        \n    }\n    public int top() {\n        \n    }\n    public int getMin() {\n        \n    }\n}`,
    cpp:    `class MinStack {\npublic:\n    MinStack() {\n        \n    }\n    void push(int val) {\n        \n    }\n    void pop() {\n        \n    }\n    int top() {\n        \n    }\n    int getMin() {\n        \n    }\n};`,
    python: `class MinStack:\n    def __init__(self):\n        pass\n    def push(self, val: int) -> None:\n        pass\n    def pop(self) -> None:\n        pass\n    def top(self) -> int:\n        pass\n    def getMin(self) -> int:\n        pass`,
  },
  'Rotting Oranges (BFS)': {
    java:   `class Solution {\n    public int orangesRotting(int[][] grid) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int orangesRotting(vector<vector<int>>& grid) {\n        \n    }\n};`,
    python: `class Solution:\n    def orangesRotting(self, grid: List[List[int]]) -> int:\n        pass`,
  },
  'Stock Span Problem': {
    java:   `class Solution {\n    public int[] calculateSpan(int[] price, int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    vector<int> calculateSpan(vector<int>& price, int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def calculateSpan(self, price: List[int], n: int) -> List[int]:\n        pass`,
  },

  // ── String ────────────────────────────────────────────────────────────────
  'Reverse Words in a String': {
    java:   `class Solution {\n    public String reverseWords(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    string reverseWords(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def reverseWords(self, s: str) -> str:\n        pass`,
  },
  'Longest Palindromic Substring': {
    java:   `class Solution {\n    public String longestPalindrome(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    string longestPalindrome(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def longestPalindrome(self, s: str) -> str:\n        pass`,
  },
  'Roman to Integer': {
    java:   `class Solution {\n    public int romanToInt(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int romanToInt(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def romanToInt(self, s: str) -> int:\n        pass`,
  },
  'String to Integer (atoi)': {
    java:   `class Solution {\n    public int myAtoi(String s) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int myAtoi(string s) {\n        \n    }\n};`,
    python: `class Solution:\n    def myAtoi(self, s: str) -> int:\n        pass`,
  },
  'Longest Common Prefix': {
    java:   `class Solution {\n    public String longestCommonPrefix(String[] strs) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    string longestCommonPrefix(vector<string>& strs) {\n        \n    }\n};`,
    python: `class Solution:\n    def longestCommonPrefix(self, strs: List[str]) -> str:\n        pass`,
  },
  'KMP / strStr': {
    java:   `class Solution {\n    public int strStr(String haystack, String needle) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int strStr(string haystack, string needle) {\n        \n    }\n};`,
    python: `class Solution:\n    def strStr(self, haystack: str, needle: str) -> int:\n        pass`,
  },
  'Valid Anagram': {
    java:   `class Solution {\n    public boolean isAnagram(String s, String t) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    bool isAnagram(string s, string t) {\n        \n    }\n};`,
    python: `class Solution:\n    def isAnagram(self, s: str, t: str) -> bool:\n        pass`,
  },
  'Count and Say': {
    java:   `class Solution {\n    public String countAndSay(int n) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    string countAndSay(int n) {\n        \n    }\n};`,
    python: `class Solution:\n    def countAndSay(self, n: int) -> str:\n        pass`,
  },
  'Compare Version Numbers': {
    java:   `class Solution {\n    public int compareVersion(String version1, String version2) {\n        \n    }\n}`,
    cpp:    `class Solution {\npublic:\n    int compareVersion(string version1, string version2) {\n        \n    }\n};`,
    python: `class Solution:\n    def compareVersion(self, version1: str, version2: str) -> int:\n        pass`,
  },
}

function getStarterCode(problem, lang) {
  return LANGUAGE_STARTERS[problem?.title]?.[lang] ?? problem?.starterCode ?? ''
}

export default function ProblemDetails() {
  const { id } = useParams()
  const { user } = useUser()
  const [problem, setProblem] = useState(null)
  const [language, setLanguage] = useState('java')
  const [code, setCode] = useState('')
  const [runResult, setRunResult] = useState(null)
  const [submitResult, setSubmitResult] = useState(null)
  const [loadingAction, setLoadingAction] = useState('')
  const [error, setError] = useState('')

  const [leftPct, setLeftPct] = useState(40)
  const [editorPct, setEditorPct] = useState(65)
  const containerRef = useRef(null)
  const rightRef = useRef(null)

  useEffect(() => {
    api
      .getProblem(id)
      .then((data) => {
        setProblem(data)
        setCode(getStarterCode(data, 'java'))
      })
      .catch((err) => setError(err.message))
  }, [id])

  const editorLanguage = useMemo(() => {
    if (language === 'cpp' || language === 'c') return 'cpp'
    if (language === 'python') return 'python'
    if (language === 'javascript') return 'javascript'
    return 'java'
  }, [language])

  function changeLanguage(nextLanguage) {
    setLanguage(nextLanguage)
    setCode(getStarterCode(problem, nextLanguage))
  }

  async function execute(kind) {
    setError('')
    setLoadingAction(kind)
    setRunResult(null)
    setSubmitResult(null)
    const payload = {
      ...getBackendUserPayload(user),
      problemId: Number(id),
      language,
      code,
      input: '',
    }
    try {
      if (kind === 'run') {
        setRunResult(await api.runCode(payload))
      } else {
        setSubmitResult(await api.submitCode(payload))
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingAction('')
    }
  }

  function startHDrag(e) {
    e.preventDefault()
    const container = containerRef.current
    if (!container) return
    document.body.style.userSelect = 'none'
    document.body.style.cursor = 'col-resize'
    function onMove(ev) {
      const rect = container.getBoundingClientRect()
      const pct = ((ev.clientX - rect.left) / rect.width) * 100
      setLeftPct(Math.min(Math.max(pct, 20), 65))
    }
    function onUp() {
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
  }

  function startVDrag(e) {
    e.preventDefault()
    const right = rightRef.current
    if (!right) return
    document.body.style.userSelect = 'none'
    document.body.style.cursor = 'row-resize'
    function onMove(ev) {
      const rect = right.getBoundingClientRect()
      const pct = ((ev.clientY - rect.top) / rect.height) * 100
      setEditorPct(Math.min(Math.max(pct, 25), 80))
    }
    function onUp() {
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
  }

  if (error && !problem) {
    return <div className="rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">{error}</div>
  }

  if (!problem) {
    return <Loading label="Loading problem" />
  }

  return (
    <div ref={containerRef} className="flex h-[calc(100vh-92px)] overflow-hidden text-slate-100">

      {/* Left: problem description */}
      <section
        style={{ width: `${leftPct}%` }}
        className="min-h-0 flex-shrink-0 overflow-hidden rounded-lg border border-[#333333] bg-[#1f1f1f]"
      >
        <div className="flex h-12 items-center gap-4 border-b border-[#333333] bg-[#303030] px-4">
          <button className="inline-flex items-center gap-2 text-sm font-semibold text-white">
            <FileText size={16} className="text-blue-400" />
            Description
          </button>
        </div>
        <div className="h-[calc(100%-48px)] space-y-6 overflow-y-auto p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-2xl font-semibold text-white">{problem.id}. {problem.title}</h2>
            <Badge className={difficultyClasses(problem.difficulty)}>{problem.difficulty}</Badge>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            {problem.tags.map((tag) => (
              <Badge key={tag} className="border-[#444444] bg-[#303030] text-slate-200">
                {tag}
              </Badge>
            ))}
          </div>

          <p className="whitespace-pre-line text-[15px] leading-7 text-slate-100">{problem.description}</p>

          <div>
            <h3 className="font-semibold text-white">Examples</h3>
            <pre className="mt-3 overflow-auto rounded-md bg-[#121212] p-4 text-sm leading-6 text-slate-100">{problem.examples}</pre>
          </div>

          <div>
            <h3 className="font-semibold text-white">Constraints</h3>
            <p className="mt-3 whitespace-pre-line text-sm leading-6 text-slate-300">{problem.constraintsText}</p>
          </div>
        </div>
      </section>

      {/* Horizontal drag divider */}
      <div
        onMouseDown={startHDrag}
        className="group mx-0.5 flex w-2 flex-shrink-0 cursor-col-resize items-center justify-center"
      >
        <div className="h-full w-0.5 rounded bg-[#333333] transition-colors group-hover:bg-blue-500/60" />
      </div>

      {/* Right: code + testcase */}
      <section ref={rightRef} className="flex flex-1 flex-col overflow-hidden">

        {/* Code editor */}
        <div
          style={{ height: `${editorPct}%` }}
          className="flex-shrink-0 overflow-hidden rounded-lg border border-[#333333] bg-[#1f1f1f]"
        >
          <div className="flex h-12 flex-wrap items-center justify-between gap-3 border-b border-[#333333] bg-[#303030] px-4">
            <div className="inline-flex items-center gap-2 text-sm font-semibold text-white">
              <TerminalSquare size={17} className="text-emerald-400" />
              Code
            </div>
            <div className="flex items-center gap-2">
              <select
                value={language}
                onChange={(event) => changeLanguage(event.target.value)}
                className="h-9 rounded-md border border-[#4a4a4a] bg-[#262626] px-3 text-sm font-medium text-slate-100 outline-none"
              >
                {languages.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
              <button
                type="button"
                disabled={loadingAction !== ''}
                onClick={() => execute('run')}
                className="inline-flex h-9 items-center gap-2 rounded-md border border-[#4a4a4a] bg-[#262626] px-4 text-sm font-semibold text-slate-100 hover:bg-[#363636] disabled:cursor-not-allowed disabled:opacity-60"
              >
                <Play size={16} />
                {loadingAction === 'run' ? 'Running' : 'Run'}
              </button>
              <button
                type="button"
                disabled={loadingAction !== ''}
                onClick={() => execute('submit')}
                className="inline-flex h-9 items-center gap-2 rounded-md bg-emerald-600 px-4 text-sm font-semibold text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <Send size={16} />
                {loadingAction === 'submit' ? 'Submitting' : 'Submit'}
              </button>
            </div>
          </div>

          <Editor
            height="calc(100% - 48px)"
            language={editorLanguage}
            value={code}
            theme="vs-dark"
            onChange={(value) => setCode(value || '')}
            options={{
              minimap: { enabled: false },
              fontSize: 14,
              lineHeight: 22,
              scrollBeyondLastLine: false,
              automaticLayout: true,
              padding: { top: 12 },
            }}
          />
        </div>

        {/* Vertical drag divider */}
        <div
          onMouseDown={startVDrag}
          className="group my-0.5 flex h-2 flex-shrink-0 cursor-row-resize items-center justify-center"
        >
          <div className="h-0.5 w-full rounded bg-[#333333] transition-colors group-hover:bg-blue-500/60" />
        </div>

        {/* Testcase + result */}
        <div className="grid min-h-0 flex-1 overflow-hidden rounded-lg border border-[#333333] bg-[#1f1f1f] md:grid-cols-[0.95fr_1.05fr]">
          <div className="min-h-0 border-b border-[#333333] md:border-r md:border-b-0">
            <div className="flex h-12 items-center gap-2 border-b border-[#333333] bg-[#303030] px-4 text-sm font-semibold text-white">
              <CheckCircle2 size={16} className="text-emerald-400" />
              Testcase
            </div>
            <div className="space-y-3 p-4">
              <div className="inline-flex rounded-md bg-[#303030] px-3 py-1 text-xs font-semibold text-slate-200">Case 1</div>
              <pre className="h-32 overflow-auto rounded-md bg-[#121212] p-3 text-sm leading-6 text-slate-100">{formatVisibleExample(problem.examples)}</pre>
            </div>
          </div>
          <div className="min-h-0">
            <div className="flex h-12 items-center gap-2 border-b border-[#333333] bg-[#303030] px-4 text-sm font-semibold text-white">
              <TerminalSquare size={16} className="text-emerald-400" />
              Test Result
            </div>
            <div className="h-[calc(100%-48px)] overflow-y-auto p-4">
              {error ? <p className="text-sm text-rose-400">{error}</p> : null}
              {runResult ? <ResultPanel result={runResult} /> : null}
              {submitResult ? <SubmissionPanel submission={submitResult} /> : null}
              {!runResult && !submitResult && !error ? (
                <p className="mt-10 text-center text-sm text-slate-500">Run code to see the result.</p>
              ) : null}
            </div>
          </div>
        </div>

      </section>
    </div>
  )
}

function ErrorBlock({ label, text, color = 'red' }) {
  const styles = {
    red:    { label: 'text-red-400',    pre: 'bg-[#1a0808] border border-red-900/50 text-red-300' },
    amber:  { label: 'text-amber-400',  pre: 'bg-[#1a1200] border border-amber-900/50 text-amber-200' },
    yellow: { label: 'text-yellow-400', pre: 'bg-[#171200] border border-yellow-900/50 text-yellow-200' },
    green:  { label: 'text-emerald-400',pre: 'bg-[#121212] text-slate-100' },
  }
  const s = styles[color] || styles.red
  if (!text || !text.trim()) return null
  return (
    <div className="space-y-1">
      <p className={`text-xs font-semibold uppercase tracking-wide ${s.label}`}>{label}</p>
      <pre className={`max-h-48 overflow-auto rounded-md p-3 text-sm leading-6 ${s.pre}`}>{text.trim()}</pre>
    </div>
  )
}

function ResultPanel({ result }) {
  const hasError = result.compileOutput || result.stderr
  return (
    <div className="space-y-3">
      <Badge className={statusClasses(result.status)}>{statusLabel(result.status)}</Badge>

      <ErrorBlock label="Compilation Error" text={result.compileOutput} color="red" />
      <ErrorBlock label="Runtime Error / stderr" text={result.stderr} color="amber" />

      {result.stdout ? (
        <ErrorBlock label="Output" text={result.stdout} color="green" />
      ) : !hasError ? (
        <pre className="max-h-36 overflow-auto rounded-md bg-[#121212] p-3 text-sm text-slate-500">
          {result.message || 'No output'}
        </pre>
      ) : null}

      <p className="text-xs text-slate-500">
        Runtime: {result.runtime ?? 0}s &nbsp;|&nbsp; Memory: {result.memory ?? 0} KB
      </p>
    </div>
  )
}

function SubmissionPanel({ submission }) {
  const isError = submission.status !== 'ACCEPTED'
  return (
    <div className="space-y-3">
      <Badge className={statusClasses(submission.status)}>{statusLabel(submission.status)}</Badge>

      {submission.errorOutput ? (
        <ErrorBlock
          label={submission.status === 'COMPILATION_ERROR' ? 'Compilation Error' :
                 submission.status === 'WRONG_ANSWER'      ? 'Wrong Answer — diff' :
                 submission.status === 'RUNTIME_ERROR'     ? 'Runtime Error' : 'Error'}
          text={submission.errorOutput}
          color={submission.status === 'WRONG_ANSWER' ? 'yellow' :
                 submission.status === 'RUNTIME_ERROR' ? 'amber' : 'red'}
        />
      ) : isError ? (
        <p className="rounded-md border border-slate-700 bg-[#1a1a1a] p-3 text-sm text-slate-400">
          {submission.status === 'TIME_LIMIT_EXCEEDED'
            ? 'Your solution exceeded the time limit. Try optimising the time complexity.'
            : submission.status === 'RUNTIME_ERROR'
            ? 'Your solution crashed during execution. Check for null dereferences, index out of bounds, or stack overflow.'
            : 'No additional details available.'}
        </p>
      ) : null}

      <p className="text-xs text-slate-500">
        Runtime: {submission.runtime ?? 0}s &nbsp;|&nbsp; Memory: {submission.memory ?? 0} KB
      </p>
    </div>
  )
}

function formatVisibleExample(examples = '') {
  const match = examples.match(/Input:\s*([\s\S]*?)\s*Output:/i)
  return match ? match[1].trim() : examples
}
