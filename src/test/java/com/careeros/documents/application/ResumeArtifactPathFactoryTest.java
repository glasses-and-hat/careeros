package com.careeros.documents.application;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeArtifactPathFactoryTest {

    private final ResumeArtifactPathFactory factory = new ResumeArtifactPathFactory();

    @Test
    void createsReadableVersionedArtifactPaths() {
        UUID jobId = UUID.fromString("44dbd6ef-44be-4a09-87de-77accfd372c0");

        var paths = factory.create(Path.of("/data/resumes"), "Block (Square)",
                LocalDate.of(2026, 7, 18), jobId, 7, "Rahul Yellapragada");

        assertThat(paths.docx()).isEqualTo(Path.of("/data/resumes/Block_Square/2026-07-18/"
                + jobId + "/v0007/Rahul_Yellapragada.docx"));
        assertThat(paths.pdf()).isEqualTo(Path.of("/data/resumes/Block_Square/2026-07-18/"
                + jobId + "/v0007/Rahul_Yellapragada.pdf"));
    }

    @Test
    void removesTraversalAndUnsafeFilenameCharacters() {
        var paths = factory.create(Path.of("/data/resumes"), "../../Acme / Labs",
                LocalDate.of(2026, 7, 18), UUID.randomUUID(), 1, "../Rahul:Resume");

        assertThat(paths.docx().normalize().toString()).startsWith("/data/resumes/Acme_Labs/");
        assertThat(paths.docx().getFileName().toString()).isEqualTo("Rahul_Resume.docx");
    }
}
