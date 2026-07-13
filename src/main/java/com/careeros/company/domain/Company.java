package com.careeros.company.domain;

import com.careeros.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

/**
 * A company being monitored for new job postings. This is the aggregate
 * root for the "company" bounded context; {@code JobPosting} instances
 * reference a {@code Company} but are owned by the job posting context.
 */
@Entity
@Table(name = "companies")
public class Company extends AuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "career_url", nullable = false)
    private String careerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ats_type", nullable = false, length = 32)
    private AtsType atsType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private Priority priority;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected Company() {
        // required by JPA
    }

    private Company(String name, String careerUrl, AtsType atsType, Priority priority, boolean enabled) {
        this.name = requireNonBlank(name, "name");
        this.careerUrl = requireNonBlank(careerUrl, "careerUrl");
        this.atsType = Objects.requireNonNull(atsType, "atsType must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
        this.enabled = enabled;
    }

    public static Company create(String name, String careerUrl, AtsType atsType, Priority priority, boolean enabled) {
        return new Company(name, careerUrl, atsType, priority, enabled);
    }

    public void updateDetails(String name, String careerUrl, AtsType atsType, Priority priority) {
        this.name = requireNonBlank(name, "name");
        this.careerUrl = requireNonBlank(careerUrl, "careerUrl");
        this.atsType = Objects.requireNonNull(atsType, "atsType must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCareerUrl() {
        return careerUrl;
    }

    public AtsType getAtsType() {
        return atsType;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Company{id=%s, name='%s', atsType=%s, priority=%s, enabled=%s}"
                .formatted(id, name, atsType, priority, enabled);
    }
}
