package com.careeros.job.infrastructure.ingestion.lever;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record LeverPosting(
        String id,
        String text,
        LeverCategories categories,
        @JsonProperty("workplaceType") String workplaceType,
        @JsonProperty("createdAt") Long createdAt,
        @JsonProperty("hostedUrl") String hostedUrl,
        @JsonProperty("applyUrl") String applyUrl,
        @JsonProperty("descriptionPlain") String descriptionPlain
) {
}
