package com.careeros.preference.web;

import com.careeros.preference.application.UserPreferenceCommand;
import com.careeros.preference.domain.UserPreference;

final class UserPreferenceMapper {

    private UserPreferenceMapper() {
    }

    static UserPreferenceResponse toResponse(UserPreference preference) {
        return new UserPreferenceResponse(
                preference.getId(),
                preference.getRoles(),
                preference.getTechnologies(),
                preference.getLocations(),
                preference.getMinimumScore(),
                preference.isRemoteOnly(),
                preference.getSalaryMin(), preference.getSalaryMax(), preference.getSalaryCurrency(),
                preference.getIgnoredCompanies(), preference.getIgnoredKeywords(), preference.isVisaSponsorshipPreferred(),
                preference.isUnitedStatesOnly(),
                preference.getCreatedAt(),
                preference.getUpdatedAt());
    }

    static UserPreferenceCommand toCommand(UserPreferenceRequest request) {
        return new UserPreferenceCommand(
                request.roles(),
                request.technologies(),
                request.locations(),
                request.minimumScore(),
                request.remoteOnly(), request.salaryMin(), request.salaryMax(), request.salaryCurrency(),
                request.ignoredCompanies(), request.ignoredKeywords(), request.visaSponsorshipPreferred(),
                request.unitedStatesOnly());
    }
}
