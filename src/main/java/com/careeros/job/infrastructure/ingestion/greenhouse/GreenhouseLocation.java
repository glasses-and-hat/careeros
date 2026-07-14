package com.careeros.job.infrastructure.ingestion.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record GreenhouseLocation(String name) {
}
