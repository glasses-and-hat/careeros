package com.careeros.job.infrastructure.persistence;

import com.careeros.job.domain.EmploymentType;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.JobPostingFilter;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Translates a {@link JobPostingFilter} into a composable JPA {@link Specification}.
 */
final class JobPostingSpecifications {

    private JobPostingSpecifications() {
    }

    static Specification<JobPosting> fromFilter(JobPostingFilter filter) {
        return Specification
                .where(fetchCompany())
                .and(hasCompanyId(filter.companyId()))
                .and(titleContains(filter.title()))
                .and(locationContains(filter.location()))
                .and(hasEmploymentType(filter.employmentType()))
                .and(isRemote(filter.remote()))
                .and(postedAfter(filter.postedAfter()));
    }

    /**
     * {@code company} is lazy and open-in-view is disabled, so list results
     * need it eagerly loaded here. Skipped for count queries, where fetches
     * are not meaningful and Hibernate would ignore them anyway.
     */
    private static Specification<JobPosting> fetchCompany() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("company", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    private static Specification<JobPosting> hasCompanyId(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    private static Specification<JobPosting> titleContains(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<JobPosting> locationContains(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    private static Specification<JobPosting> hasEmploymentType(EmploymentType employmentType) {
        if (employmentType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("employmentType"), employmentType);
    }

    private static Specification<JobPosting> isRemote(Boolean remote) {
        if (remote == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("remote"), remote);
    }

    private static Specification<JobPosting> postedAfter(LocalDate postedAfter) {
        if (postedAfter == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("postedDate"), postedAfter);
    }
}
