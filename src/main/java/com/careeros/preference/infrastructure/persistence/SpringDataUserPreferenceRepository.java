package com.careeros.preference.infrastructure.persistence;

import com.careeros.preference.domain.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataUserPreferenceRepository extends JpaRepository<UserPreference, UUID> {
}
