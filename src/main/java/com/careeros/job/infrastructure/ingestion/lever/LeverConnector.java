package com.careeros.job.infrastructure.ingestion.lever;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.ingestion.AtsConnector;
import com.careeros.job.infrastructure.ingestion.support.AtsDateParser;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import com.careeros.job.infrastructure.ingestion.support.RemoteHeuristic;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Fetches job postings from Lever's public Postings API
 * ({@code GET /v0/postings/{site}?mode=json}). {@code ats_identifier} is the
 * Lever site slug, e.g. {@code "acme"}. The response is a bare JSON array,
 * not wrapped in an object.
 */
@Component
class LeverConnector implements AtsConnector {

    private final RestClient restClient;

    LeverConnector(RestClient.Builder atsRestClientBuilder, IngestionProperties properties) {
        this.restClient = atsRestClientBuilder.baseUrl(properties.leverBaseUrl()).build();
    }

    @Override
    public AtsType supportedType() {
        return AtsType.LEVER;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        List<LeverPosting> postings = restClient.get()
                .uri("/{site}?mode=json", company.getAtsIdentifier())
                .retrieve()
                .body(new ParameterizedTypeReference<List<LeverPosting>>() {
                });

        if (postings == null) {
            return List.of();
        }
        return postings.stream().map(posting -> toCommand(company, posting)).toList();
    }

    private JobPostingCommand toCommand(Company company, LeverPosting posting) {
        String location = posting.categories() == null ? null : posting.categories().location();
        String commitment = posting.categories() == null ? null : posting.categories().commitment();
        boolean remote = "remote".equalsIgnoreCase(posting.workplaceType())
                || (posting.workplaceType() == null && RemoteHeuristic.looksRemote(location));
        String applyUrl = posting.applyUrl() != null ? posting.applyUrl() : posting.hostedUrl();

        return new JobPostingCommand(
                posting.id(),
                company.getId(),
                posting.text(),
                location,
                EmploymentTypeParser.fromKeyword(commitment),
                remote,
                null, null, null,
                posting.descriptionPlain(),
                AtsDateParser.fromEpochMillis(posting.createdAt()),
                applyUrl);
    }
}
