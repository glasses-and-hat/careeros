package com.careeros.preference.domain;

import com.careeros.common.domain.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A user's job search preferences, used to score and filter discovered job
 * postings. Milestone 1 treats this as a single-tenant aggregate; a future
 * milestone will attach it to an authenticated user account.
 */
@Entity
@Table(name = "user_preferences")
public class UserPreference extends AuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preference_roles", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Column(name = "role", nullable = false)
    private List<String> roles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preference_technologies", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Column(name = "technology", nullable = false)
    private List<String> technologies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preference_locations", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Column(name = "location", nullable = false)
    private List<String> locations = new ArrayList<>();

    @Column(name = "minimum_score", nullable = false)
    private int minimumScore;

    @Column(name = "remote_only", nullable = false)
    private boolean remoteOnly;

    protected UserPreference() {
        // required by JPA
    }

    private UserPreference(List<String> roles, List<String> technologies, List<String> locations,
                            int minimumScore, boolean remoteOnly) {
        this.roles = new ArrayList<>(requireNonNull(roles, "roles"));
        this.technologies = new ArrayList<>(requireNonNull(technologies, "technologies"));
        this.locations = new ArrayList<>(requireNonNull(locations, "locations"));
        this.minimumScore = requireInRange(minimumScore);
        this.remoteOnly = remoteOnly;
    }

    public static UserPreference create(List<String> roles, List<String> technologies, List<String> locations,
                                         int minimumScore, boolean remoteOnly) {
        return new UserPreference(roles, technologies, locations, minimumScore, remoteOnly);
    }

    public void update(List<String> roles, List<String> technologies, List<String> locations,
                        int minimumScore, boolean remoteOnly) {
        this.roles = new ArrayList<>(requireNonNull(roles, "roles"));
        this.technologies = new ArrayList<>(requireNonNull(technologies, "technologies"));
        this.locations = new ArrayList<>(requireNonNull(locations, "locations"));
        this.minimumScore = requireInRange(minimumScore);
        this.remoteOnly = remoteOnly;
    }

    private static List<String> requireNonNull(List<String> value, String field) {
        return Objects.requireNonNull(value, field + " must not be null");
    }

    private static int requireInRange(int minimumScore) {
        if (minimumScore < 0 || minimumScore > 100) {
            throw new IllegalArgumentException("minimumScore must be between 0 and 100");
        }
        return minimumScore;
    }

    public UUID getId() {
        return id;
    }

    public List<String> getRoles() {
        return List.copyOf(roles);
    }

    public List<String> getTechnologies() {
        return List.copyOf(technologies);
    }

    public List<String> getLocations() {
        return List.copyOf(locations);
    }

    public int getMinimumScore() {
        return minimumScore;
    }

    public boolean isRemoteOnly() {
        return remoteOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPreference other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserPreference{id=%s, roles=%s, minimumScore=%d, remoteOnly=%s}"
                .formatted(id, roles, minimumScore, remoteOnly);
    }
}
