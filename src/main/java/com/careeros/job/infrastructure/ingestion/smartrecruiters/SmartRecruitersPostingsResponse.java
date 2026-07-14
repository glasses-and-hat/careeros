package com.careeros.job.infrastructure.ingestion.smartrecruiters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record SmartRecruitersPostingsResponse(
        int totalFound,
        int offset,
        int limit,
        List<SmartRecruitersPosting> content
) {
}
