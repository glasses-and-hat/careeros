package com.careeros.company.infrastructure.persistence;

import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the {@link CompanyRepository} port on top of Spring
 * Data JPA. This is the only class in the company module aware of Spring
 * Data's {@code JpaRepository} / {@code Specification} APIs.
 */
@Repository
class CompanyRepositoryAdapter implements CompanyRepository {

    private final SpringDataCompanyRepository jpaRepository;

    CompanyRepositoryAdapter(SpringDataCompanyRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Company save(Company company) {
        return jpaRepository.save(company);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Page<Company> findAll(CompanyFilter filter, Pageable pageable) {
        return jpaRepository.findAll(CompanySpecifications.fromFilter(filter), pageable);
    }

    @Override
    public List<Company> findAllEnabled() {
        return jpaRepository.findAllByEnabledTrue();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
