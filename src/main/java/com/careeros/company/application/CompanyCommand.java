package com.careeros.company.application;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Priority;

/**
 * Application-layer input for creating or updating a {@link com.careeros.company.domain.Company}.
 * Deliberately decoupled from web request DTOs so the service layer has no
 * dependency on the web layer.
 */
public record CompanyCommand(
        String name,
        String careerUrl,
        AtsType atsType,
        Priority priority,
        boolean enabled
) {
}
