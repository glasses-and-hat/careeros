package com.careeros.preference.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record UserPreferenceResponse(
        UUID id,
        List<String> roles,
        List<String> technologies,
        List<String> locations,
        int minimumScore,
        boolean remoteOnly,
        BigDecimal salaryMin, BigDecimal salaryMax, String salaryCurrency,
        List<String> ignoredCompanies, List<String> ignoredKeywords, boolean visaSponsorshipPreferred,
        boolean unitedStatesOnly,
        Instant createdAt,
        Instant updatedAt
) {
}
