package com.careeros.job.application.ingestion;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;

import java.util.List;
import java.util.UUID;

/**
 * Outcome of running ingestion for a single company: how many postings were
 * newly created, how many were already-known duplicates (expected on every
 * poll, not a failure), and how many failed to persist for some other reason.
 */
public record CompanyIngestionResult(
        UUID companyId,
        String companyName,
        AtsType atsType,
        int created,
        int skipped,
        int failed,
        List<String> errors
) {

    static CompanyIngestionResult unsupportedAtsType(Company company) {
        return new CompanyIngestionResult(company.getId(), company.getName(), company.getAtsType(), 0, 0, 1,
                List.of("monitoring provider is not configured for " + company.getName()));
    }

    static CompanyIngestionResult fetchFailed(Company company, String message) {
        return new CompanyIngestionResult(company.getId(), company.getName(), company.getAtsType(), 0, 0, 0,
                List.of("fetch failed: " + message));
    }
}
