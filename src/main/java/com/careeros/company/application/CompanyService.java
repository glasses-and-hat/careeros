package com.careeros.company.application;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use cases for managing monitored companies. This is the only entry point
 * the web layer uses to interact with the company aggregate.
 */
@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company create(CompanyCommand command) {
        if (companyRepository.existsByNameIgnoreCase(command.name())) {
            throw new DuplicateResourceException("A company named '%s' already exists".formatted(command.name()));
        }
        Company company = Company.create(
                command.name(), command.careerUrl(), command.atsType(), command.priority(), command.enabled());
        return companyRepository.save(company);
    }

    public Company update(UUID id, CompanyCommand command) {
        Company company = getOrThrow(id);
        company.updateDetails(command.name(), command.careerUrl(), command.atsType(), command.priority());
        if (command.enabled()) {
            company.enable();
        } else {
            company.disable();
        }
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public Company get(UUID id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Page<Company> list(CompanyFilter filter, Pageable pageable) {
        return companyRepository.findAll(filter, pageable);
    }

    @Transactional(readOnly = true)
    public List<Company> listEnabled() {
        return companyRepository.findAllEnabled();
    }

    public void delete(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Company", id);
        }
        companyRepository.deleteById(id);
    }

    private Company getOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Company", id));
    }
}
