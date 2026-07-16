package com.careeros.preference.application;

import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.preference.domain.UserPreference;
import com.careeros.preference.domain.UserPreferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public UserPreference create(UserPreferenceCommand command) {
        UserPreference preference = UserPreference.create(
                command.roles(), command.technologies(), command.locations(),
                command.minimumScore(), command.remoteOnly());
        expand(preference, command);
        return userPreferenceRepository.save(preference);
    }

    public UserPreference update(UUID id, UserPreferenceCommand command) {
        UserPreference preference = getOrThrow(id);
        preference.update(command.roles(), command.technologies(), command.locations(),
                command.minimumScore(), command.remoteOnly());
        expand(preference, command);
        return userPreferenceRepository.save(preference);
    }

    private static void expand(UserPreference p, UserPreferenceCommand c) {
        p.expand(c.salaryMin(), c.salaryMax(), c.salaryCurrency(), safe(c.ignoredCompanies()),
                safe(c.ignoredKeywords()), c.visaSponsorshipPreferred());
    }
    private static <T> java.util.List<T> safe(java.util.List<T> v) { return v == null ? java.util.List.of() : v; }

    @Transactional(readOnly = true)
    public UserPreference get(UUID id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Page<UserPreference> list(Pageable pageable) {
        return userPreferenceRepository.findAll(pageable);
    }

    public void delete(UUID id) {
        if (!userPreferenceRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("UserPreference", id);
        }
        userPreferenceRepository.deleteById(id);
    }

    private UserPreference getOrThrow(UUID id) {
        return userPreferenceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("UserPreference", id));
    }
}
