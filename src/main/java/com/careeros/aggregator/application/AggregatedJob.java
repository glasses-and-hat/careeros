package com.careeros.aggregator.application;

import com.careeros.job.domain.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Provider-neutral job discovered through a secondary job-market source. */
public record AggregatedJob(
        String externalId,
        String companyName,
        String companyUrl,
        String title,
        String location,
        EmploymentType employmentType,
        boolean remote,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        String description,
        LocalDate postedDate,
        String applyUrl,
        String sourceUrl
) {
}
