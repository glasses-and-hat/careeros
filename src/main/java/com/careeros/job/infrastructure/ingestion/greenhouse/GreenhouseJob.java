package com.careeros.job.infrastructure.ingestion.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record GreenhouseJob(
        Long id,
        String title,
        @JsonProperty("first_published") String firstPublished,
        @JsonProperty("absolute_url") String absoluteUrl,
        String content,
        GreenhouseLocation location
) {
}
