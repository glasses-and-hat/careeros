package com.careeros.job.infrastructure.ingestion.workday;

import java.util.Map;

record WorkdayJobsRequest(Map<String, Object> appliedFacets, int limit, int offset, String searchText) {

    static WorkdayJobsRequest page(int limit, int offset) {
        return new WorkdayJobsRequest(Map.of(), limit, offset, "");
    }
}
