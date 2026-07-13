package com.careeros.company.domain;

/**
 * Applicant Tracking System backing a company's careers page. Determines
 * which connector (future work) is used to discover job postings.
 */
public enum AtsType {
    GREENHOUSE,
    LEVER,
    ASHBY,
    WORKDAY,
    SMARTRECRUITERS,
    OTHER
}
