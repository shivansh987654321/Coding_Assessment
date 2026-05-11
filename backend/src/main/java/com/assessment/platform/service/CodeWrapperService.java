package com.assessment.platform.service;

import com.assessment.platform.entity.Problem;
import org.springframework.stereotype.Service;

@Service
public class CodeWrapperService {

    public String wrap(Problem problem, String language, String userCode) {
        String lang = language == null ? "java" : language.toLowerCase();
        if ("Two Sum".equalsIgnoreCase(problem.getTitle())) {
            return switch (lang) {
                case "cpp"    -> twoSumCpp(userCode);
                case "python" -> twoSumPython(userCode);
                default       -> twoSumJava(userCode);
            };
        }
        if ("Valid Palindrome".equalsIgnoreCase(problem.getTitle())) {
            return switch (lang) {
                case "cpp"    -> palindromeCpp(userCode);
                case "python" -> palindromePython(userCode);
                default       -> palindromeJava(userCode);
            };
        }
        return userCode;
    }

    // ── Two Sum ───────────────────────────────────────────────────────────────

    private String twoSumJava(String userCode) {
        return """
                import java.util.*;

                %s

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                        int target = sc.nextInt();
                        int[] answer = new Solution().twoSum(nums, target);
                        if (answer == null || answer.length < 2) {
                            System.out.println("");
                        } else {
                            System.out.println(answer[0] + " " + answer[1]);
                        }
                    }
                }
                """.formatted(userCode);
    }

    private String twoSumCpp(String userCode) {
        return """
                #include <bits/stdc++.h>
                using namespace std;

                %s

                int main() {
                    int n;
                    cin >> n;
                    vector<int> nums(n);
                    for (int i = 0; i < n; i++) cin >> nums[i];
                    int target;
                    cin >> target;
                    Solution sol;
                    vector<int> ans = sol.twoSum(nums, target);
                    cout << ans[0] << " " << ans[1] << endl;
                    return 0;
                }
                """.formatted(userCode);
    }

    private String twoSumPython(String userCode) {
        return """
                from typing import List

                %s

                import sys

                def main():
                    data = sys.stdin.read().split()
                    n = int(data[0])
                    nums = [int(data[i + 1]) for i in range(n)]
                    target = int(data[n + 1])
                    result = Solution().twoSum(nums, target)
                    print(result[0], result[1])

                main()
                """.formatted(userCode);
    }

    // ── Valid Palindrome ──────────────────────────────────────────────────────

    private String palindromeJava(String userCode) {
        return """
                import java.util.*;

                %s

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        String s = sc.hasNextLine() ? sc.nextLine() : "";
                        System.out.println(new Solution().isPalindrome(s));
                    }
                }
                """.formatted(userCode);
    }

    private String palindromeCpp(String userCode) {
        return """
                #include <bits/stdc++.h>
                using namespace std;

                %s

                int main() {
                    string s;
                    getline(cin, s);
                    Solution sol;
                    cout << (sol.isPalindrome(s) ? "true" : "false") << endl;
                    return 0;
                }
                """.formatted(userCode);
    }

    private String palindromePython(String userCode) {
        return """
                %s

                import sys

                def main():
                    s = sys.stdin.readline().rstrip('\\n')
                    result = Solution().isPalindrome(s)
                    print(str(result).lower())

                main()
                """.formatted(userCode);
    }
}
