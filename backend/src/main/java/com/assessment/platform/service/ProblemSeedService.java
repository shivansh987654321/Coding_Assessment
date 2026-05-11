package com.assessment.platform.service;

import com.assessment.platform.dto.ProblemResponse;
import com.assessment.platform.entity.Difficulty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class ProblemSeedService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    public ProblemResponse randomByDifficulty(Difficulty difficulty) {
        String resource = switch (difficulty) {
            case MEDIUM -> "/problems_all_medium.json";
            case HARD -> "/problems_all_hard.json";
            default -> "/problems_all_easy.json";
        };

        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is == null) return seedFallback(difficulty);
            List<Map<String, Object>> list = mapper.readValue(is, new TypeReference<>() {});
            if (list.isEmpty()) return seedFallback(difficulty);
            Map<String, Object> entry = list.get(random.nextInt(list.size()));
            String title = safeString(entry.get("title"));
            String url = safeString(entry.get("url"));
            String statement = safeString(entry.get("statement"));
            List<Map<String,String>> samples = (List<Map<String,String>>) entry.getOrDefault("samples", Collections.emptyList());
            String examples = renderExamples(samples);
            String description = statement.isBlank() ? generateTemplate(title, url) : statement;

            return new ProblemResponse(
                    null,
                    title,
                    description,
                    difficulty,
                    null,
                    examples,
                    null,
                    Collections.emptyList(),
                    Instant.now()
            );
        } catch (Exception e) {
            return seedFallback(difficulty);
        }
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String renderExamples(List<Map<String,String>> samples) {
        if (samples == null || samples.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map<String,String> s : samples) {
            sb.append("Input: ").append(s.getOrDefault("input", "")).append("\n");
            sb.append("Output: ").append(s.getOrDefault("output", "")).append("\n");
            if (s.containsKey("explanation")) sb.append("Explanation: ").append(s.get("explanation")).append("\n");
            sb.append("---\n");
        }
        return sb.toString();
    }

    private String generateTemplate(String title, String url) {
        return String.format("Problem: %s\nSource: %s\n\nDescription: Implement the problem titled '%s'. Use standard input/output or function signature as appropriate.\n\nNote: examples and constraints may be available at the source URL.", title, url, title);
    }

    private ProblemResponse seedFallback(Difficulty difficulty) {
        return new ProblemResponse(
                null,
                "Example Problem",
                "This is a fallback seeded problem. Replace with real data.",
                difficulty,
                null,
                "",
                null,
                Collections.emptyList(),
                Instant.now()
        );
    }
}
