package com.careeros.job.infrastructure.persistence;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.Priority;
import com.careeros.job.domain.EmploymentType;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.SalaryRange;
import com.careeros.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the JobPosting persistence mapping, including the deterministic
 * hash used for duplicate detection, against a real PostgreSQL instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobPostingRepositoryIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private SpringDataJobPostingRepository jobPostingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Company persistedCompany() {
        Company company = Company.create(
                "Acme Inc " + System.nanoTime(), "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);
        return entityManager.persistAndFlush(company);
    }

    private static JobPosting aJobPosting(Company company, String externalId) {
        return JobPosting.create(externalId, company, "Senior Backend Engineer", "Remote",
                EmploymentType.FULL_TIME, true, SalaryRange.undisclosed(), "Great job", LocalDate.now(),
                "https://acme.example/apply/" + externalId);
    }

    @Test
    void savesAndReloadsJobPostingWithComputedHash() {
        Company company = persistedCompany();
        JobPosting saved = jobPostingRepository.save(aJobPosting(company, "job-1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getHash()).isNotBlank();

        JobPosting reloaded = jobPostingRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(reloaded.getCompany().getId()).isEqualTo(company.getId());
    }

    @Test
    void reloadedJobPostingNeverExposesNullSalary() {
        // Regression test: when every salary column is NULL, Hibernate maps the
        // embeddable itself to null rather than an all-null instance. JobPosting
        // normalizes this in its getter; verify that survives a real round-trip.
        Company company = persistedCompany();
        JobPosting saved = jobPostingRepository.saveAndFlush(aJobPosting(company, "job-4"));
        entityManagerClear();

        JobPosting reloaded = jobPostingRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getSalary()).isNotNull();
        assertThat(reloaded.getSalary().isDisclosed()).isFalse();
    }

    private void entityManagerClear() {
        entityManager.flush();
        entityManager.getEntityManager().clear();
    }

    @Test
    void sameCompanyAndExternalIdProduceSameHash() {
        Company company = persistedCompany();
        jobPostingRepository.saveAndFlush(aJobPosting(company, "job-2"));

        JobPosting duplicate = aJobPosting(company, "job-2");
        assertThat(jobPostingRepository.existsByHash(duplicate.getHash())).isTrue();
    }

    @Test
    void rejectsDuplicateHashAtDatabaseLevel() {
        Company company = persistedCompany();
        jobPostingRepository.saveAndFlush(aJobPosting(company, "job-3"));

        JobPosting duplicate = aJobPosting(company, "job-3");
        assertThatThrownBy(() -> jobPostingRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
