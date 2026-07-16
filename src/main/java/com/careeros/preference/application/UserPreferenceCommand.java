package com.careeros.preference.application;

import java.util.List;
import java.math.BigDecimal;

public record UserPreferenceCommand(
        List<String> roles,
        List<String> technologies,
        List<String> locations,
        int minimumScore,
        boolean remoteOnly,
        BigDecimal salaryMin, BigDecimal salaryMax, String salaryCurrency,
        List<String> ignoredCompanies, List<String> ignoredKeywords, boolean visaSponsorshipPreferred,
        boolean unitedStatesOnly
) {
    public UserPreferenceCommand(List<String> roles, List<String> technologies, List<String> locations,
                                 int minimumScore, boolean remoteOnly) {
        this(roles, technologies, locations, minimumScore, remoteOnly, null, null, null,
                List.of(), List.of(), false, false);
    }
}
