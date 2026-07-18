package com.careeros.job.infrastructure.ingestion.workday;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.ingestion.AtsConnector;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import com.careeros.job.infrastructure.ingestion.support.RemoteHeuristic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches job postings from a Workday tenant's CXS API
 * ({@code POST https://{tenant}.myworkdayjobs.com/wday/cxs/{tenant}/{site}/jobs}).
 * {@code ats_identifier} is the compound value {@code "tenant/site"}, e.g.
 * {@code "acme/External"}.
 *
 * <p>Highest-risk connector of the 5: Workday tenants sometimes live on
 * numbered shard subdomains ({@code wd1.myworkdayjobs.com}) rather than the
 * bare host used by default. Those companies configure a public host through
 * {@code provider_configuration}, for example
 * {@code {"host":"nvidia.wd5.myworkdayjobs.com"}}. Hosts are restricted to
 * the configured Workday domain suffix.
 *
 * <p>{@code postedOn} is a relative string ("Posted Today", "Posted 3 Days
 * Ago") rather than a real date, so {@code postedDate} is deliberately left
 * {@code null} here instead of parsed — a heuristic parse would drift with
 * ingestion timing and "30+ Days Ago" is unbounded/ambiguous. The column is
 * nullable, so this is a safe, honest omission rather than a fragile guess.
 */
@Component
class WorkdayConnector implements AtsConnector {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_POSTINGS = 1000;

    private final RestClient restClient;
    private final String hostSuffix;
    private final ObjectMapper objectMapper;

    WorkdayConnector(RestClient.Builder atsRestClientBuilder, IngestionProperties properties,
                     ObjectMapper objectMapper) {
        this.restClient = atsRestClientBuilder.build();
        this.hostSuffix = properties.workdayHostSuffix();
        this.objectMapper = objectMapper;
    }

    @Override
    public AtsType supportedType() {
        return AtsType.WORKDAY;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        String[] tenantAndSite = company.getAtsIdentifier().split("/", 2);
        if (tenantAndSite.length != 2) {
            throw new IllegalStateException(
                    "Workday ats_identifier must be in 'tenant/site' form, got: " + company.getAtsIdentifier());
        }
        String tenant = tenantAndSite[0];
        String site = tenantAndSite[1];
        String baseHost = "https://" + configuredHost(company, tenant);
        String jobsUrl = baseHost + "/wday/cxs/" + tenant + "/" + site + "/jobs";

        List<JobPostingCommand> commands = new ArrayList<>();
        int offset = 0;
        int total = Integer.MAX_VALUE;

        while (offset < total && offset < MAX_POSTINGS) {
            WorkdayJobsResponse response = restClient.post()
                    .uri(jobsUrl)
                    .body(WorkdayJobsRequest.page(PAGE_SIZE, offset))
                    .retrieve()
                    .body(WorkdayJobsResponse.class);

            if (response == null || response.jobPostings() == null || response.jobPostings().isEmpty()) {
                break;
            }
            response.jobPostings().forEach(posting -> commands.add(toCommand(company, baseHost, posting)));
            total = response.total();
            offset += PAGE_SIZE;
        }
        return commands;
    }

    private String configuredHost(Company company, String tenant) {
        String host = tenant + "." + hostSuffix;
        if (company.getProviderConfiguration() != null && !company.getProviderConfiguration().isBlank()) {
            try {
                String configured = objectMapper.readTree(company.getProviderConfiguration()).path("host").asText();
                if (!configured.isBlank()) host = configured;
            } catch (Exception e) {
                throw new IllegalStateException("Invalid Workday provider configuration", e);
            }
        }
        String normalized = host.toLowerCase(java.util.Locale.ROOT);
        if (!host.matches("[A-Za-z0-9.-]+")
                || !(normalized.equals(hostSuffix) || normalized.endsWith("." + hostSuffix))) {
            throw new IllegalStateException("Workday host must be within " + hostSuffix + ": " + host);
        }
        return host;
    }

    private JobPostingCommand toCommand(Company company, String baseHost, WorkdayJobPosting posting) {
        String externalId = externalId(posting);
        String applyUrl = posting.externalPath() == null ? null : baseHost + posting.externalPath();

        return new JobPostingCommand(
                externalId,
                company.getId(),
                posting.title(),
                posting.locationsText(),
                EmploymentTypeParser.fromKeyword(posting.title()),
                RemoteHeuristic.looksRemote(posting.title(), posting.locationsText()),
                null, null, null,
                null,
                null,
                applyUrl);
    }

    private static String externalId(WorkdayJobPosting posting) {
        if (posting.bulletFields() != null && !posting.bulletFields().isEmpty()) {
            return posting.bulletFields().get(0);
        }
        if (posting.externalPath() == null) {
            return null;
        }
        String[] segments = posting.externalPath().split("/");
        return segments.length == 0 ? null : segments[segments.length - 1];
    }
}
