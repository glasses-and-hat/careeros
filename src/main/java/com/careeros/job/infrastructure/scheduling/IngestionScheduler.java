package com.careeros.job.infrastructure.scheduling;

import com.careeros.job.application.ingestion.IngestionSummary;
import com.careeros.job.application.ingestion.JobIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Periodically runs ingestion for all enabled companies. The bean (and its
 * {@code @Scheduled} trigger) only exists when {@code careeros.ingestion.enabled}
 * is {@code true} — disabled means no scheduling activity at all, not a
 * silently no-op-ing trigger, so ingestion never fires unexpectedly in
 * dev/test/CI.
 */
@Component
@ConditionalOnProperty(prefix = "careeros.ingestion", name = "enabled", havingValue = "true")
class IngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    private final JobIngestionService jobIngestionService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    IngestionScheduler(JobIngestionService jobIngestionService) {
        this.jobIngestionService = jobIngestionService;
    }

    @Scheduled(fixedDelayString = "${careeros.ingestion.poll-interval}")
    void pollEnabledCompanies() {
        if (!running.compareAndSet(false, true)) {
            log.info("Skipping scheduled ingestion run — previous run still in progress");
            return;
        }
        try {
            IngestionSummary summary = jobIngestionService.ingestAllEnabledCompanies();
            log.info("Scheduled ingestion run complete: {} companies, {} created, {} skipped, {} failed",
                    summary.companiesProcessed(), summary.totalCreated(), summary.totalSkipped(),
                    summary.totalFailed());
        } finally {
            running.set(false);
        }
    }
}
