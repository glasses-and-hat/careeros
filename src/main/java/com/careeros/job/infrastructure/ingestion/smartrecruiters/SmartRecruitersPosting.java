package com.careeros.job.infrastructure.ingestion.smartrecruiters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record SmartRecruitersPosting(
        String id,
        String name,
        @JsonProperty("releasedDate") String releasedDate,
        SmartRecruitersLocation location,
        @JsonProperty("typeOfEmployment") TypeOfEmployment typeOfEmployment,
        String ref
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TypeOfEmployment(String label) {
    }
}
