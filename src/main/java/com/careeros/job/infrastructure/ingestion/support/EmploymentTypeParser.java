package com.careeros.job.infrastructure.ingestion.support;

import com.careeros.job.domain.EmploymentType;

import java.util.Locale;

/**
 * Maps free-text employment-type labels from various ATS APIs (which don't
 * share a common enum) to {@link EmploymentType} via case-insensitive
 * keyword matching. Returns {@code null} on no match rather than throwing —
 * {@code JobPosting.employmentType} is a nullable column.
 */
public final class EmploymentTypeParser {

    private EmploymentTypeParser() {
    }

    public static EmploymentType fromKeyword(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("intern")) {
            return EmploymentType.INTERNSHIP;
        }
        if (lower.contains("part")) {
            return EmploymentType.PART_TIME;
        }
        if (lower.contains("contract")) {
            return EmploymentType.CONTRACT;
        }
        if (lower.contains("temp")) {
            return EmploymentType.TEMPORARY;
        }
        if (lower.contains("full")) {
            return EmploymentType.FULL_TIME;
        }
        return null;
    }
}
