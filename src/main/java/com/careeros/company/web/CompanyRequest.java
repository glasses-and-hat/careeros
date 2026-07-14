package com.careeros.company.web;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 2048) String careerUrl,
        @NotNull AtsType atsType,
        @NotNull Priority priority,
        boolean enabled,
        @Size(max = 255) String atsIdentifier
) {
}
