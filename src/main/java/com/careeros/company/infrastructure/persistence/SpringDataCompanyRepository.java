package com.careeros.company.infrastructure.persistence;

import com.careeros.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Company}. Package-private surface
 * area: application code depends on {@link com.careeros.company.domain.CompanyRepository}
 * only, never on this interface directly.
 */
interface SpringDataCompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    boolean existsByNameIgnoreCase(String name);

    List<Company> findAllByEnabledTrue();
}
