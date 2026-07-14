package com.careeros.job.infrastructure.ingestion.ashby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record AshbyJobBoardResponse(List<AshbyJob> jobs) {
}
