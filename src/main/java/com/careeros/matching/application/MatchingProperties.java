package com.careeros.matching.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("careeros.matching.weights")
public record MatchingProperties(int role, int technology, int location, int remote, int companyPriority, int recency) {
    public MatchingProperties {
        if (role + technology + location + remote + companyPriority + recency != 100)
            throw new IllegalArgumentException("matching weights must total 100");
    }
}
