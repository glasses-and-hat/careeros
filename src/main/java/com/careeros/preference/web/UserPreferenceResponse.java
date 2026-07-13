package com.careeros.preference.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserPreferenceResponse(
        UUID id,
        List<String> roles,
        List<String> technologies,
        List<String> locations,
        int minimumScore,
        boolean remoteOnly,
        Instant createdAt,
        Instant updatedAt
) {
}
