package com.careeros.job.application;

import com.careeros.job.domain.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Application-layer input for creating or updating a {@link com.careeros.job.domain.JobPosting}.
 */
public record JobPostingCommand(
        String externalId,
        UUID companyId,
        String title,
        String location,
        EmploymentType employmentType,
        boolean remote,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        String description,
        LocalDate postedDate,
        String applyUrl
) {
}
