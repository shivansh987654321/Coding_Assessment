package com.assessment.platform.config;

import java.util.ArrayList;
import java.util.List;

final class SeedTestCases {
    private SeedTestCases() {
    }

    record CaseData(String input, String expectedOutput, boolean hidden) {
    }

    static List<CaseData> twoSumCases() {
        List<CaseData> cases = new ArrayList<>();
        cases.add(new CaseData("4\n2 7 11 15\n9\n", "0 1\n", false));
        cases.add(new CaseData("3\n3 2 4\n6\n", "1 2\n", true));
        cases.add(new CaseData("2\n3 3\n6\n", "0 1\n", true));

        for (int i = 1; i <= 47; i++) {
            int first = i * 1000 + 3;
            int second = i * 1000 + 7;
            int target = first + second;
            String input = "6\n"
                    + i + " "
                    + first + " "
                    + (-i) + " "
                    + (42 + i) + " "
                    + second + " "
                    + (-100 - i) + "\n"
                    + target + "\n";
            cases.add(new CaseData(input, "1 4\n", true));
        }

        return cases;
    }

    static List<CaseData> validPalindromeCases() {
        List<CaseData> cases = new ArrayList<>();
        cases.add(new CaseData("A man, a plan, a canal: Panama\n", "true\n", false));
        cases.add(new CaseData("race a car\n", "false\n", true));

        String[] palindromeInputs = {
                "",
                " ",
                ".",
                "a",
                "aa",
                "ab_a",
                "No lemon, no melon",
                "Was it a car or a cat I saw?",
                "Madam, I'm Adam",
                "Never odd or even",
                "Able was I ere I saw Elba",
                "Step on no pets",
                "Eva, can I see bees in a cave?",
                "Mr. Owl ate my metal worm",
                "Do geese see God?",
                "Yo, banana boy!",
                "12321",
                "1a2b2a1",
                "Red rum, sir, is murder",
                "Top spot",
                "Borrow or rob?",
                "Live not on evil",
                "No 'x' in Nixon",
                "Too hot to hoot",
                "123abccba321"
        };

        for (String input : palindromeInputs) {
            cases.add(new CaseData(input + "\n", "true\n", true));
        }

        String[] nonPalindromeInputs = {
                "hello",
                "OpenAI",
                "abc",
                "ab",
                "0P",
                "palindrome",
                "coding assessment",
                "almost a palindrome",
                "1231",
                "A Toyota",
                "This is not one",
                "abcda",
                "leetcode",
                "Java Spring Boot",
                "front end",
                "not even close",
                "race a cars",
                "abcdefg",
                "1a2",
                "two pointers",
                "Panama plan",
                "No lemon, one melon",
                "Was it a car?",
                "Step on pets"
        };

        for (String input : nonPalindromeInputs) {
            cases.add(new CaseData(input + "\n", "false\n", true));
        }

        if (cases.size() != 51) {
            throw new IllegalStateException("Expected 51 Valid Palindrome seed cases, found " + cases.size());
        }

        return cases;
    }
}
