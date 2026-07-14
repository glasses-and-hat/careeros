package com.careeros.job.infrastructure.ingestion.smartrecruiters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record SmartRecruitersLocation(String city, String region, String country, Boolean remote) {
}
