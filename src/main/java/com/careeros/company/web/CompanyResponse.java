package com.careeros.company.web;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Priority;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import com.careeros.provider.domain.ProviderType;

public record CompanyResponse(
        UUID id,
        String name,
        String careerUrl,
        AtsType atsType,
        Priority priority,
        boolean enabled,
        String atsIdentifier,
        ProviderType providerType,
        String providerConfiguration,
        List<ProviderType> fallbackProviders,
        Instant createdAt,
        Instant updatedAt
) {
}
