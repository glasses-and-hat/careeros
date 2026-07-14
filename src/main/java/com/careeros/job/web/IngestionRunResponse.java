package com.careeros.job.web;

import java.util.List;

public record IngestionRunResponse(
        int companiesProcessed,
        int totalCreated,
        int totalSkipped,
        int totalFailed,
        List<CompanyIngestionResultResponse> results
) {
}
