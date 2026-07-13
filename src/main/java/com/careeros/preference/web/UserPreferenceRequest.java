package com.careeros.preference.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserPreferenceRequest(
        @NotNull List<@NotNull String> roles,
        @NotNull List<@NotNull String> technologies,
        @NotNull List<@NotNull String> locations,
        @Min(0) @Max(100) int minimumScore,
        boolean remoteOnly
) {
}
