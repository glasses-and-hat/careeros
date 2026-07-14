package com.careeros.job.infrastructure.ingestion.lever;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record LeverCategories(String location, String commitment) {
}
