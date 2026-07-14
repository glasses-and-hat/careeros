package com.careeros.job.application.ingestion;

import java.util.List;

/**
 * Aggregate result of an ingestion run across one or more companies.
 */
public record IngestionSummary(
        int companiesProcessed,
        int totalCreated,
        int totalSkipped,
        int totalFailed,
        List<CompanyIngestionResult> results
) {

    static IngestionSummary of(List<CompanyIngestionResult> results) {
        int created = results.stream().mapToInt(CompanyIngestionResult::created).sum();
        int skipped = results.stream().mapToInt(CompanyIngestionResult::skipped).sum();
        int failed = results.stream().mapToInt(CompanyIngestionResult::failed).sum();
        return new IngestionSummary(results.size(), created, skipped, failed, results);
    }
}
