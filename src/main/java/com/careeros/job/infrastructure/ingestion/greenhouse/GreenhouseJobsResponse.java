package com.careeros.job.infrastructure.ingestion.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record GreenhouseJobsResponse(List<GreenhouseJob> jobs) {
}
