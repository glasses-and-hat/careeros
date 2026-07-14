package com.careeros.job.infrastructure.ingestion.greenhouse;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.ingestion.AtsConnector;
import com.careeros.job.infrastructure.ingestion.support.AtsDateParser;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import com.careeros.job.infrastructure.ingestion.support.RemoteHeuristic;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Fetches job postings from Greenhouse's public Job Board API
 * ({@code GET /v1/boards/{token}/jobs?content=true}). {@code ats_identifier}
 * is the board token, e.g. {@code "acme"}.
 *
 * <p>Greenhouse's base schema exposes no explicit remote flag or employment
 * type, so both are derived heuristically from the title/location text.
 */
@Component
class GreenhouseConnector implements AtsConnector {

    private final RestClient restClient;

    GreenhouseConnector(RestClient.Builder atsRestClientBuilder, IngestionProperties properties) {
        this.restClient = atsRestClientBuilder.baseUrl(properties.greenhouseBaseUrl()).build();
    }

    @Override
    public AtsType supportedType() {
        return AtsType.GREENHOUSE;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        GreenhouseJobsResponse response = restClient.get()
                .uri("/{token}/jobs?content=true", company.getAtsIdentifier())
                .retrieve()
                .body(GreenhouseJobsResponse.class);

        if (response == null || response.jobs() == null) {
            return List.of();
        }
        return response.jobs().stream().map(job -> toCommand(company, job)).toList();
    }

    private JobPostingCommand toCommand(Company company, GreenhouseJob job) {
        String locationName = job.location() == null ? null : job.location().name();
        return new JobPostingCommand(
                job.id() == null ? null : String.valueOf(job.id()),
                company.getId(),
                job.title(),
                locationName,
                EmploymentTypeParser.fromKeyword(job.title()),
                RemoteHeuristic.looksRemote(job.title(), locationName),
                null, null, null,
                job.content(),
                AtsDateParser.fromIsoInstant(job.firstPublished()),
                job.absoluteUrl());
    }
}
