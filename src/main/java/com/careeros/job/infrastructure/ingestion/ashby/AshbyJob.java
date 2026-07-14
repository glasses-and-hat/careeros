package com.careeros.job.infrastructure.ingestion.ashby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record AshbyJob(
        String id,
        String title,
        String location,
        @JsonProperty("isRemote") Boolean isRemote,
        @JsonProperty("publishedAt") String publishedAt,
        @JsonProperty("employmentType") String employmentType,
        @JsonProperty("applyUrl") String applyUrl,
        @JsonProperty("jobUrl") String jobUrl,
        @JsonProperty("descriptionHtml") String descriptionHtml
) {
}
