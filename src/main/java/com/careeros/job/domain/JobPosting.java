package com.careeros.job.domain;

import com.careeros.common.domain.AuditableEntity;
import com.careeros.company.domain.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * A single job opening discovered on a company's careers page, normalized
 * into CareerOS's common domain model regardless of which ATS it came from.
 *
 * <p>{@code hash} is a content fingerprint of (company, externalId) used for
 * duplicate detection when the same job is re-scraped; it is deterministic
 * so re-ingesting an unchanged posting never creates a second row.
 */
@Entity
@Table(name = "job_postings")
public class JobPosting extends AuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32)
    private EmploymentType employmentType;

    @Column(name = "remote", nullable = false)
    private boolean remote;

    @Embedded
    private SalaryRange salary;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "apply_url", nullable = false)
    private String applyUrl;

    @Column(name = "hash", nullable = false, unique = true, length = 64)
    private String hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private JobSourceType sourceType;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    protected JobPosting() {
        // required by JPA
    }

    private JobPosting(String externalId, Company company, String title, String location,
                        EmploymentType employmentType, boolean remote, SalaryRange salary,
                        String description, LocalDate postedDate, String applyUrl,
                        JobSourceType sourceType, String sourceUrl) {
        this.externalId = requireNonBlank(externalId, "externalId");
        this.company = Objects.requireNonNull(company, "company must not be null");
        this.title = requireNonBlank(title, "title");
        this.location = location;
        this.employmentType = employmentType;
        this.remote = remote;
        this.salary = salary == null ? SalaryRange.undisclosed() : salary;
        this.description = description;
        this.postedDate = postedDate;
        this.applyUrl = requireNonBlank(applyUrl, "applyUrl");
        this.sourceType = sourceType == null ? JobSourceType.DIRECT_PROVIDER : sourceType;
        this.sourceUrl = sourceUrl;
        this.hash = computeHash(company.getId(), this.externalId);
    }

    public static JobPosting create(String externalId, Company company, String title, String location,
                                     EmploymentType employmentType, boolean remote, SalaryRange salary,
                                     String description, LocalDate postedDate, String applyUrl) {
        return new JobPosting(externalId, company, title, location, employmentType, remote, salary,
                description, postedDate, applyUrl, JobSourceType.DIRECT_PROVIDER, null);
    }

    public static JobPosting create(String externalId, Company company, String title, String location,
                                     EmploymentType employmentType, boolean remote, SalaryRange salary,
                                     String description, LocalDate postedDate, String applyUrl,
                                     JobSourceType sourceType, String sourceUrl) {
        return new JobPosting(externalId, company, title, location, employmentType, remote, salary,
                description, postedDate, applyUrl, sourceType, sourceUrl);
    }

    public void updateDetails(String title, String location, EmploymentType employmentType, boolean remote,
                               SalaryRange salary, String description, LocalDate postedDate, String applyUrl) {
        this.title = requireNonBlank(title, "title");
        this.location = location;
        this.employmentType = employmentType;
        this.remote = remote;
        this.salary = salary == null ? SalaryRange.undisclosed() : salary;
        this.description = description;
        this.postedDate = postedDate;
        this.applyUrl = requireNonBlank(applyUrl, "applyUrl");
    }

    /**
     * Deterministic fingerprint of (companyId, externalId). Two postings with
     * the same company and the ATS's external id always collapse to the same
     * hash, which is the basis for duplicate detection during re-ingestion.
     */
    private static String computeHash(UUID companyId, String externalId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((companyId + "|" + externalId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
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

    public String getExternalId() {
        return externalId;
    }

    public Company getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public boolean isRemote() {
        return remote;
    }

    /**
     * Never null: when every column of the embedded salary is NULL in the
     * database, Hibernate maps the embeddable itself to null rather than an
     * all-null instance, so this normalizes that back to {@link SalaryRange#undisclosed()}.
     */
    public SalaryRange getSalary() {
        return salary == null ? SalaryRange.undisclosed() : salary;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public String getApplyUrl() {
        return applyUrl;
    }

    public String getHash() {
        return hash;
    }

    public JobSourceType getSourceType() {
        return sourceType == null ? JobSourceType.DIRECT_PROVIDER : sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobPosting other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "JobPosting{id=%s, externalId='%s', title='%s', remote=%s}"
                .formatted(id, externalId, title, remote);
    }
}
