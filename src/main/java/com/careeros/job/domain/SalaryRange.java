package com.careeros.job.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Optional compensation range advertised on a job posting. All fields are
 * nullable since many postings do not disclose salary information.
 *
 * <p>Deliberately a plain class rather than a record: Hibernate 6 resolves
 * embeddable attributes alphabetically by name (currency, max, min) but
 * invokes a record's canonical constructor positionally (min, max,
 * currency), which silently mismatches arguments and throws a
 * {@code ClassCastException} at load time. A plain class with an explicit
 * all-args constructor sidesteps that ordering assumption entirely.
 */
@Embeddable
public class SalaryRange {

    @Column(name = "salary_min")
    private final BigDecimal min;

    @Column(name = "salary_max")
    private final BigDecimal max;

    @Column(name = "salary_currency", length = 3)
    private final String currency;

    protected SalaryRange() {
        this.min = null;
        this.max = null;
        this.currency = null;
    }

    public SalaryRange(BigDecimal min, BigDecimal max, String currency) {
        this.min = min;
        this.max = max;
        this.currency = currency;
    }

    public static SalaryRange undisclosed() {
        return new SalaryRange(null, null, null);
    }

    public boolean isDisclosed() {
        return min != null || max != null;
    }

    public BigDecimal min() {
        return min;
    }

    public BigDecimal max() {
        return max;
    }

    public String currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SalaryRange other)) return false;
        return Objects.equals(min, other.min) && Objects.equals(max, other.max) && Objects.equals(currency, other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, currency);
    }

    @Override
    public String toString() {
        return "SalaryRange{min=%s, max=%s, currency='%s'}".formatted(min, max, currency);
    }
}
