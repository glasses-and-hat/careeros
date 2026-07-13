package com.careeros.company.domain;

/**
 * How aggressively a company should be monitored for new postings.
 * Future scheduling logic will poll HIGH priority companies more frequently.
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
