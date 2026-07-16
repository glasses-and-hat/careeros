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
import java.util.ArrayList;
import java.util.List;
import com.careeros.provider.domain.ProviderType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

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

    @Column(name = "ats_identifier")
    private String atsIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", length = 32)
    private ProviderType providerType;

    @Column(name = "provider_configuration", columnDefinition = "TEXT")
    private String providerConfiguration;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_fallback_providers", joinColumns = @JoinColumn(name = "company_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private List<ProviderType> fallbackProviders = new ArrayList<>();

    protected Company() {
        // required by JPA
    }

    private Company(String name, String careerUrl, AtsType atsType, Priority priority, boolean enabled,
                     String atsIdentifier) {
        this.name = requireNonBlank(name, "name");
        this.careerUrl = requireNonBlank(careerUrl, "careerUrl");
        this.atsType = Objects.requireNonNull(atsType, "atsType must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
        this.enabled = enabled;
        this.atsIdentifier = requireAtsIdentifierIfNeeded(atsType, atsIdentifier);
        this.providerType = atsType == AtsType.OTHER ? null : ProviderType.valueOf(atsType.name());
    }

    public static Company create(String name, String careerUrl, AtsType atsType, Priority priority, boolean enabled,
                                  String atsIdentifier) {
        return new Company(name, careerUrl, atsType, priority, enabled, atsIdentifier);
    }

    public void updateDetails(String name, String careerUrl, AtsType atsType, Priority priority, String atsIdentifier) {
        this.name = requireNonBlank(name, "name");
        this.careerUrl = requireNonBlank(careerUrl, "careerUrl");
        this.atsType = Objects.requireNonNull(atsType, "atsType must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
        this.atsIdentifier = requireAtsIdentifierIfNeeded(atsType, atsIdentifier);
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void configureProvider(ProviderType providerType, String providerConfiguration,
                                  List<ProviderType> fallbackProviders) {
        this.providerType = providerType;
        this.providerConfiguration = providerConfiguration;
        this.fallbackProviders = new ArrayList<>(fallbackProviders == null ? List.of() : fallbackProviders);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    /**
     * Every ATS type except {@code OTHER} needs a connector-specific
     * identifier (board token, site slug, tenant/site, ...) to be ingestible;
     * enforced here so it holds for any caller, not just the web layer.
     */
    private static String requireAtsIdentifierIfNeeded(AtsType atsType, String atsIdentifier) {
        if (atsType != AtsType.OTHER && (atsIdentifier == null || atsIdentifier.isBlank())) {
            throw new IllegalArgumentException("atsIdentifier must not be blank for atsType " + atsType);
        }
        return atsIdentifier;
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

    public String getAtsIdentifier() {
        return atsIdentifier;
    }
    public ProviderType getProviderType() { return providerType; }
    public String getProviderConfiguration() { return providerConfiguration; }
    public List<ProviderType> getFallbackProviders() { return List.copyOf(fallbackProviders); }

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
