package com.careeros.job.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Search criteria for listing job postings. All fields are optional; a null
 * field means "do not filter on this attribute".
 */
public record JobPostingFilter(
        UUID companyId,
        String title,
        String location,
        EmploymentType employmentType,
        Boolean remote,
        LocalDate postedAfter
) {

    public static JobPostingFilter empty() {
        return new JobPostingFilter(null, null, null, null, null, null);
    }
}
