package com.careeros.preference.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port defining the persistence operations the application layer needs for
 * {@link UserPreference}. Implemented by an infrastructure adapter.
 */
public interface UserPreferenceRepository {

    UserPreference save(UserPreference userPreference);

    Optional<UserPreference> findById(UUID id);

    Page<UserPreference> findAll(Pageable pageable);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
