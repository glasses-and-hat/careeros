package com.careeros.job.infrastructure.ingestion.workday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record WorkdayJobPosting(
        String title,
        @JsonProperty("externalPath") String externalPath,
        @JsonProperty("locationsText") String locationsText,
        @JsonProperty("postedOn") String postedOn,
        @JsonProperty("bulletFields") List<String> bulletFields
) {
}
