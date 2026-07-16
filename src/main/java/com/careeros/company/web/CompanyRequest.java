package com.careeros.company.web;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.careeros.provider.domain.ProviderType;
import java.util.List;

public record CompanyRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 2048) String careerUrl,
        @NotNull AtsType atsType,
        @NotNull Priority priority,
        boolean enabled,
        @Size(max = 255) String atsIdentifier,
        ProviderType providerType,
        String providerConfiguration,
        List<ProviderType> fallbackProviders
) {
    public CompanyRequest(String name, String careerUrl, AtsType atsType, Priority priority,
                          boolean enabled, String atsIdentifier) {
        this(name, careerUrl, atsType, priority, enabled, atsIdentifier, null, null, List.of());
    }
}
