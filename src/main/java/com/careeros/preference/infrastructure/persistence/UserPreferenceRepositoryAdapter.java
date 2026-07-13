package com.careeros.preference.infrastructure.persistence;

import com.careeros.preference.domain.UserPreference;
import com.careeros.preference.domain.UserPreferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class UserPreferenceRepositoryAdapter implements UserPreferenceRepository {

    private final SpringDataUserPreferenceRepository jpaRepository;

    UserPreferenceRepositoryAdapter(SpringDataUserPreferenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserPreference save(UserPreference userPreference) {
        return jpaRepository.save(userPreference);
    }

    @Override
    public Optional<UserPreference> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<UserPreference> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
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
