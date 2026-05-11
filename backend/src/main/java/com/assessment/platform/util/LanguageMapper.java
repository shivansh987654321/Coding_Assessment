package com.assessment.platform.util;

import java.util.Locale;
import java.util.Map;

public final class LanguageMapper {
    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "java", 62,
            "javascript", 63,
            "python", 71,
            "cpp", 54,
            "c", 50
    );

    private LanguageMapper() {
    }

    public static Integer toJudge0Id(String language) {
        if (language == null) {
            return LANGUAGE_IDS.get("java");
        }
        return LANGUAGE_IDS.getOrDefault(language.toLowerCase(Locale.ROOT), LANGUAGE_IDS.get("java"));
    }
}
