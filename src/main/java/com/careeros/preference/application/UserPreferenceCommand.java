package com.careeros.preference.application;

import java.util.List;

public record UserPreferenceCommand(
        List<String> roles,
        List<String> technologies,
        List<String> locations,
        int minimumScore,
        boolean remoteOnly
) {
}
