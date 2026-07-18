package com.careeros.company.infrastructure.persistence;

import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Translates a {@link CompanyFilter} into a composable JPA {@link Specification}.
 */
final class CompanySpecifications {

    private CompanySpecifications() {
    }

    static Specification<Company> fromFilter(CompanyFilter filter) {
        return Specification
                .where(nameContains(filter.name()))
                .and(hasAtsType(filter.atsType()))
                .and(hasPriority(filter.priority()))
                .and(hasProviderType(filter.providerType()))
                .and(isEnabled(filter.enabled()));
    }

    private static Specification<Company> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Company> hasAtsType(com.careeros.company.domain.AtsType atsType) {
        if (atsType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("atsType"), atsType);
    }

    private static Specification<Company> hasPriority(com.careeros.company.domain.Priority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    private static Specification<Company> hasProviderType(com.careeros.provider.domain.ProviderType providerType) {
        if (providerType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("providerType"), providerType);
    }

    private static Specification<Company> isEnabled(Boolean enabled) {
        if (enabled == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }
}
