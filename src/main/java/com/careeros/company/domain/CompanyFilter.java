package com.careeros.company.domain;

import com.careeros.provider.domain.ProviderType;

/**
 * Search criteria for listing companies. All fields are optional; a null
 * field means "do not filter on this attribute".
 */
public record CompanyFilter(
        String name,
        AtsType atsType,
        Priority priority,
        ProviderType providerType,
        Boolean enabled
) {

    public static CompanyFilter empty() {
        return new CompanyFilter(null, null, null, null, null);
    }
}
