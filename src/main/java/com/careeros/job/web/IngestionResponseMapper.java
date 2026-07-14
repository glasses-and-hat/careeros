package com.careeros.job.web;

import com.careeros.job.application.ingestion.CompanyIngestionResult;
import com.careeros.job.application.ingestion.IngestionSummary;

final class IngestionResponseMapper {

    private IngestionResponseMapper() {
    }

    static IngestionRunResponse toResponse(IngestionSummary summary) {
        return new IngestionRunResponse(
                summary.companiesProcessed(),
                summary.totalCreated(),
                summary.totalSkipped(),
                summary.totalFailed(),
                summary.results().stream().map(IngestionResponseMapper::toResponse).toList());
    }

    private static CompanyIngestionResultResponse toResponse(CompanyIngestionResult result) {
        return new CompanyIngestionResultResponse(
                result.companyId(),
                result.companyName(),
                result.atsType(),
                result.created(),
                result.skipped(),
                result.failed(),
                result.errors());
    }
}
