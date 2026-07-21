package com.careeros.validation.application;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeGroundingValidatorTest {
    private final ResumeGroundingValidator validator = new ResumeGroundingValidator();

    @Test
    void acceptsFactsPresentInMaster() {
        assertThat(validator.validate("Built Java services improving latency by 20%",
                List.of("Improved Java service latency by 20%")).valid()).isTrue();
    }

    @Test
    void rejectsUnknownTechnologyAndMetric() {
        var result = validator.validate("Built Java services",
                List.of("Built Kubernetes platform reducing cost by 40%"));
        assertThat(result.valid()).isFalse();
        assertThat(result.warnings()).contains("Unknown technology: kubernetes", "Unknown metric: 40%");
    }

    @Test
    void rejectsBlankBulletsBeforeTheyCanReplaceMasterContent() {
        var result = validator.validate("Core Competencies: Java, Spring",
                Arrays.asList("Backend engineering", "  ", null));

        assertThat(result.valid()).isFalse();
        assertThat(result.warnings()).contains("Generated bullet 2 is blank", "Generated bullet 3 is blank");
    }

    @Test
    void rejectsMissingBulletCollection() {
        assertThat(validator.validate("Master resume", List.of()).valid()).isFalse();
        assertThat(validator.validate("Master resume", null).warnings()).contains("No generated bullets returned");
    }
}
