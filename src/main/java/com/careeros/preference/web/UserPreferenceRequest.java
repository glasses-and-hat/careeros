package com.careeros.preference.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.math.BigDecimal;

public record UserPreferenceRequest(
        @NotNull List<@NotNull String> roles,
        @NotNull List<@NotNull String> technologies,
        @NotNull List<@NotNull String> locations,
        @Min(0) @Max(100) int minimumScore,
        boolean remoteOnly,
        BigDecimal salaryMin, BigDecimal salaryMax, String salaryCurrency,
        List<String> ignoredCompanies, List<String> ignoredKeywords, boolean visaSponsorshipPreferred,
        boolean unitedStatesOnly
) {
    public UserPreferenceRequest(List<String> roles, List<String> technologies, List<String> locations,
                                 int minimumScore, boolean remoteOnly) {
        this(roles, technologies, locations, minimumScore, remoteOnly, null, null, null,
                List.of(), List.of(), false, false);
    }
}
