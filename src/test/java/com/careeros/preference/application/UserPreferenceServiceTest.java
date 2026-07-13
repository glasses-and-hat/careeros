package com.careeros.preference.application;

import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.preference.domain.UserPreference;
import com.careeros.preference.domain.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    private UserPreferenceService userPreferenceService;

    @BeforeEach
    void setUp() {
        userPreferenceService = new UserPreferenceService(userPreferenceRepository);
    }

    @Test
    void createsUserPreference() {
        UserPreferenceCommand command = new UserPreferenceCommand(
                List.of("Backend Engineer"), List.of("Java", "Kotlin"), List.of("Remote"), 70, true);
        when(userPreferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreference result = userPreferenceService.create(command);

        assertThat(result.getRoles()).containsExactly("Backend Engineer");
        assertThat(result.getMinimumScore()).isEqualTo(70);
        assertThat(result.isRemoteOnly()).isTrue();
    }

    @Test
    void throwsWhenPreferenceNotFound() {
        UUID id = UUID.randomUUID();
        when(userPreferenceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPreferenceService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsMinimumScoreOutOfRange() {
        UserPreferenceCommand command = new UserPreferenceCommand(List.of(), List.of(), List.of(), 150, false);

        assertThatThrownBy(() -> userPreferenceService.create(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
