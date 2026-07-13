package com.careeros.job.web;

import com.careeros.job.domain.EmploymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobPostingResponse(
        UUID id,
        String externalId,
        UUID companyId,
        String companyName,
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
        String hash,
        Instant createdAt,
        Instant updatedAt
) {
}
