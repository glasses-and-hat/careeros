package com.careeros.job.web;

import com.careeros.job.domain.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JobPostingRequest(
        @NotBlank @Size(max = 255) String externalId,
        @NotNull UUID companyId,
        @NotBlank @Size(max = 500) String title,
        String location,
        EmploymentType employmentType,
        boolean remote,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        @Size(max = 3) String salaryCurrency,
        String description,
        LocalDate postedDate,
        @NotBlank @Size(max = 2048) String applyUrl
) {
}
