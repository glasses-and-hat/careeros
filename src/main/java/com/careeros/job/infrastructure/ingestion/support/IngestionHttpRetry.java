package com.careeros.job.infrastructure.ingestion.support;

import java.time.Duration;
import java.util.function.Supplier;

/** Bounded exponential backoff for idempotent public job-board reads. */
public final class IngestionHttpRetry {
    private IngestionHttpRetry() {
    }

    public static <T> T execute(int attempts, Duration initialBackoff, Supplier<T> request) {
        if (attempts < 1) {
            throw new IllegalArgumentException("attempts must be at least 1");
        }
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            try {
                return request.get();
            } catch (RuntimeException failure) {
                lastFailure = failure;
                if (attempt + 1 < attempts) {
                    pause(initialBackoff.multipliedBy(1L << attempt));
                }
            }
        }
        throw lastFailure;
    }

    private static void pause(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Job provider retry interrupted", interrupted);
        }
    }
}
