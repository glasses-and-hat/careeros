package com.careeros.company.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port defining the persistence operations the application layer needs for
 * {@link Company}. Implemented by an infrastructure adapter; the domain and
 * application layers depend only on this interface, never on Spring Data or
 * JPA directly.
 */
public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    boolean existsByNameIgnoreCase(String name);

    Page<Company> findAll(CompanyFilter filter, Pageable pageable);

    List<Company> findAllEnabled();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
