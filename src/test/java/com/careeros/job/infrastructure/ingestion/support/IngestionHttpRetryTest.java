package com.careeros.job.infrastructure.ingestion.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionHttpRetryTest {
    @Test
    void retriesTransientFailureAndReturnsSuccessfulResult() {
        AtomicInteger calls = new AtomicInteger();

        String result = IngestionHttpRetry.execute(3, Duration.ZERO, () -> {
            if (calls.incrementAndGet() < 3) throw new IllegalStateException("timed out");
            return "jobs";
        });

        assertThat(result).isEqualTo("jobs");
        assertThat(calls).hasValue(3);
    }

    @Test
    void propagatesFinalFailureAfterAttemptLimit() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> IngestionHttpRetry.execute(2, Duration.ZERO, () -> {
            calls.incrementAndGet();
            throw new IllegalStateException("still unavailable");
        })).isInstanceOf(IllegalStateException.class).hasMessage("still unavailable");
        assertThat(calls).hasValue(2);
    }
}
