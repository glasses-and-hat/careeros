package com.careeros.job.infrastructure.ingestion.ashby;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.ingestion.AtsConnector;
import com.careeros.job.infrastructure.ingestion.support.AtsDateParser;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Fetches job postings from Ashby's public Job Board API
 * ({@code GET /posting-api/job-board/{boardName}}). {@code ats_identifier}
 * is the Ashby job-board name, e.g. {@code "acme"}.
 *
 * <p>Ashby is the best-quality source of the 5 connectors: it exposes real
 * booleans/enums for remote status and employment type, so no heuristics are
 * needed for either field.
 */
@Component
class AshbyConnector implements AtsConnector {

    private final RestClient restClient;

    AshbyConnector(RestClient.Builder atsRestClientBuilder, IngestionProperties properties) {
        this.restClient = atsRestClientBuilder.baseUrl(properties.ashbyBaseUrl()).build();
    }

    @Override
    public AtsType supportedType() {
        return AtsType.ASHBY;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        AshbyJobBoardResponse response = restClient.get()
                .uri("/{boardName}", company.getAtsIdentifier())
                .retrieve()
                .body(AshbyJobBoardResponse.class);

        if (response == null || response.jobs() == null) {
            return List.of();
        }
        return response.jobs().stream().map(job -> toCommand(company, job)).toList();
    }

    private JobPostingCommand toCommand(Company company, AshbyJob job) {
        String applyUrl = job.applyUrl() != null ? job.applyUrl() : job.jobUrl();
        return new JobPostingCommand(
                job.id(),
                company.getId(),
                job.title(),
                job.location(),
                EmploymentTypeParser.fromKeyword(job.employmentType()),
                Boolean.TRUE.equals(job.isRemote()),
                null, null, null,
                job.descriptionHtml(),
                AtsDateParser.fromIsoInstant(job.publishedAt()),
                applyUrl);
    }
}
