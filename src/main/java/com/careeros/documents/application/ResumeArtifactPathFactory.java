package com.careeros.documents.application;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class ResumeArtifactPathFactory {

    public ArtifactPaths create(Path root, String company, LocalDate generatedDate, UUID jobId,
                                int version, String artifactBaseName) {
        Path directory = root
                .resolve(segment(company, "company"))
                .resolve(generatedDate.toString())
                .resolve(jobId.toString())
                .resolve("v%04d".formatted(version));
        String fileName = segment(artifactBaseName, "Tailored_Resume");
        return new ArtifactPaths(directory.resolve(fileName + ".docx"),
                directory.resolve(fileName + ".pdf"));
    }

    private String segment(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String safe = value.strip()
                .replaceAll("[^\\p{L}\\p{N}._-]+", "_")
                .replaceAll("^[._-]+|[._-]+$", "");
        if (safe.isBlank()) return fallback;
        return safe.substring(0, Math.min(safe.length(), 100));
    }

    public record ArtifactPaths(Path docx, Path pdf) {}
}
