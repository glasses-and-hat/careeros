package com.careeros.job.infrastructure.ingestion.workday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record WorkdayJobsResponse(int total, @JsonProperty("jobPostings") List<WorkdayJobPosting> jobPostings) {
}
