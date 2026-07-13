package com.careeros.preference.web;

import com.careeros.common.web.PageResponse;
import com.careeros.preference.application.UserPreferenceService;
import com.careeros.preference.domain.UserPreference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * CRUD API for user job-search preferences, used by the future scoring engine.
 */
@RestController
@RequestMapping("/api/v1/preferences")
@Tag(name = "Preferences", description = "Manage user job search preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @PostMapping
    @Operation(summary = "Create a user preference profile")
    public ResponseEntity<UserPreferenceResponse> create(@Valid @RequestBody UserPreferenceRequest request) {
        UserPreference preference = userPreferenceService.create(UserPreferenceMapper.toCommand(request));
        UserPreferenceResponse response = UserPreferenceMapper.toResponse(preference);
        return ResponseEntity.created(URI.create("/api/v1/preferences/" + preference.getId())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user preference profile by id")
    public UserPreferenceResponse get(@PathVariable UUID id) {
        return UserPreferenceMapper.toResponse(userPreferenceService.get(id));
    }

    @GetMapping
    @Operation(summary = "List user preference profiles with pagination and sorting")
    public PageResponse<UserPreferenceResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(userPreferenceService.list(pageable), UserPreferenceMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user preference profile")
    public UserPreferenceResponse update(@PathVariable UUID id, @Valid @RequestBody UserPreferenceRequest request) {
        UserPreference preference = userPreferenceService.update(id, UserPreferenceMapper.toCommand(request));
        return UserPreferenceMapper.toResponse(preference);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user preference profile")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userPreferenceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
