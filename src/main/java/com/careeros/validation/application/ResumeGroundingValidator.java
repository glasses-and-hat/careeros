package com.careeros.validation.application;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ResumeGroundingValidator {
    private static final Set<String> TECHNOLOGIES = Set.of("java", "spring", "spring boot", "kafka", "aws",
            "azure", "gcp", "kubernetes", "docker", "terraform", "postgresql", "mysql", "mongodb", "redis",
            "python", "javascript", "typescript", "react", "angular", "node", "go", "rust", "scala", "spark",
            "datadog", "grafana", "jenkins", "github", "gitlab");
    private static final Pattern METRIC = Pattern.compile("(?i)(?:[$€£]?\\d+(?:[.,]\\d+)?%?|\\d+[xX])");

    public Result validate(String master, List<String> bullets) {
        var warnings = new ArrayList<String>();
        if (bullets == null || bullets.isEmpty()) {
            warnings.add("No generated bullets returned");
            return new Result(false, warnings);
        }
        for (int index = 0; index < bullets.size(); index++) {
            if (bullets.get(index) == null || bullets.get(index).isBlank()) {
                warnings.add("Generated bullet " + (index + 1) + " is blank");
            }
        }

        var source = normalize(master);
        var generatedText = String.join(" ", bullets.stream().map(value -> value == null ? "" : value).toList());
        var generated = normalize(generatedText);
        for (var technology : TECHNOLOGIES) {
            if (contains(generated, technology) && !contains(source, technology)) {
                warnings.add("Unknown technology: " + technology);
            }
        }
        var matcher = METRIC.matcher(generatedText);
        while (matcher.find()) {
            if (!master.contains(matcher.group())) warnings.add("Unknown metric: " + matcher.group());
        }
        return new Result(warnings.isEmpty(), warnings.stream().distinct().toList());
    }

    private boolean contains(String text, String term) { return (" " + text + " ").contains(" " + term + " "); }
    private String normalize(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#.]+", " ").trim(); }
    public record Result(boolean valid, List<String> warnings) {}
}
