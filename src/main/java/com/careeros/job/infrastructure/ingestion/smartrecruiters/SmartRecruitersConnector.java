package com.careeros.job.infrastructure.ingestion.smartrecruiters;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.ingestion.AtsConnector;
import com.careeros.job.infrastructure.ingestion.support.AtsDateParser;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fetches job postings from SmartRecruiters' public Postings API
 * ({@code GET /v1/companies/{companyIdentifier}/postings}), which is
 * paginated via {@code offset}/{@code limit}. {@code ats_identifier} is the
 * SmartRecruiters company identifier, e.g. {@code "AcmeInc"}.
 */
@Component
class SmartRecruitersConnector implements AtsConnector {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_POSTINGS = 1000;

    private final RestClient restClient;

    SmartRecruitersConnector(RestClient.Builder atsRestClientBuilder, IngestionProperties properties) {
        this.restClient = atsRestClientBuilder.baseUrl(properties.smartRecruitersBaseUrl()).build();
    }

    @Override
    public AtsType supportedType() {
        return AtsType.SMARTRECRUITERS;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        List<JobPostingCommand> commands = new ArrayList<>();
        int offset = 0;
        int totalFound = Integer.MAX_VALUE;

        while (offset < totalFound && offset < MAX_POSTINGS) {
            SmartRecruitersPostingsResponse response = restClient.get()
                    .uri("/{companyIdentifier}/postings?offset={offset}&limit={limit}",
                            company.getAtsIdentifier(), offset, PAGE_SIZE)
                    .retrieve()
                    .body(SmartRecruitersPostingsResponse.class);

            if (response == null || response.content() == null || response.content().isEmpty()) {
                break;
            }
            response.content().forEach(posting -> commands.add(toCommand(company, posting)));
            totalFound = response.totalFound();
            offset += PAGE_SIZE;
        }
        return commands;
    }

    private JobPostingCommand toCommand(Company company, SmartRecruitersPosting posting) {
        String location = buildLocation(posting.location());
        boolean remote = posting.location() != null && Boolean.TRUE.equals(posting.location().remote());
        String employmentLabel = posting.typeOfEmployment() == null ? null : posting.typeOfEmployment().label();
        String applyUrl = posting.ref() != null ? posting.ref()
                : "https://jobs.smartrecruiters.com/" + company.getAtsIdentifier() + "/" + posting.id();

        return new JobPostingCommand(
                posting.id(),
                company.getId(),
                posting.name(),
                location,
                EmploymentTypeParser.fromKeyword(employmentLabel),
                remote,
                null, null, null,
                null,
                AtsDateParser.fromIsoInstant(posting.releasedDate()),
                applyUrl);
    }

    private static String buildLocation(SmartRecruitersLocation location) {
        if (location == null) {
            return null;
        }
        return Stream.of(location.city(), location.region(), location.country())
                .filter(part -> part != null && !part.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }
}
