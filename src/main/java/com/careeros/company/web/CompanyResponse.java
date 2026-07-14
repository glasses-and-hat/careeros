package com.careeros.company.web;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Priority;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String careerUrl,
        AtsType atsType,
        Priority priority,
        boolean enabled,
        String atsIdentifier,
        Instant createdAt,
        Instant updatedAt
) {
}
