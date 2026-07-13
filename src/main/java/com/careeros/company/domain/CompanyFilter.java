package com.careeros.company.domain;

/**
 * Search criteria for listing companies. All fields are optional; a null
 * field means "do not filter on this attribute".
 */
public record CompanyFilter(
        String name,
        AtsType atsType,
        Priority priority,
        Boolean enabled
) {

    public static CompanyFilter empty() {
        return new CompanyFilter(null, null, null, null);
    }
}
