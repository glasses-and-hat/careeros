package com.careeros.company.infrastructure.persistence;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.Priority;
import com.careeros.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Company persistence mapping, Flyway schema, and unique
 * constraint against a real PostgreSQL instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyRepositoryIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private SpringDataCompanyRepository companyRepository;

    private static Company aCompany(String name) {
        return Company.create(name, "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true, "acme");
    }

    @Test
    void savesAndReloadsCompanyWithGeneratedIdAndTimestamps() {
        Company saved = companyRepository.save(aCompany("Acme Inc"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Company reloaded = companyRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Acme Inc");
        assertThat(reloaded.getAtsType()).isEqualTo(AtsType.GREENHOUSE);
    }

    @Test
    void enforcesUniqueCompanyName() {
        companyRepository.saveAndFlush(aCompany("Unique Co"));

        assertThat(companyRepository.existsByNameIgnoreCase("unique co")).isTrue();
    }

    @Test
    void filtersCompaniesBySpecification() {
        companyRepository.save(aCompany("Enabled Greenhouse Co"));
        Company disabled = aCompany("Disabled Co");
        disabled.disable();
        companyRepository.save(disabled);

        CompanyFilter filter = new CompanyFilter(null, AtsType.GREENHOUSE, null,
                com.careeros.provider.domain.ProviderType.GREENHOUSE, true);
        Specification<Company> spec = CompanySpecifications.fromFilter(filter);
        Page<Company> result = companyRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Company::getName)
                .containsExactly("Enabled Greenhouse Co");
    }

    @Test
    void savesAndReloadsCompanyWithNullAtsIdentifier() {
        Company company = Company.create(
                "No ATS Co", "https://acme.example/careers", AtsType.OTHER, Priority.LOW, true, null);
        Company saved = companyRepository.save(company);

        Company reloaded = companyRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getAtsType()).isEqualTo(AtsType.OTHER);
        assertThat(reloaded.getAtsIdentifier()).isNull();
    }
}
