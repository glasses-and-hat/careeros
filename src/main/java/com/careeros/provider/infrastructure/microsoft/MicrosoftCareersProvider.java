package com.careeros.provider.infrastructure.microsoft;

import com.careeros.company.domain.Company;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.infrastructure.ingestion.support.EmploymentTypeParser;
import com.careeros.job.infrastructure.ingestion.support.RemoteHeuristic;
import com.careeros.provider.application.JobProvider;
import com.careeros.provider.domain.ProviderType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Company-specific adapter for Microsoft's public Eightfold/PCSX careers API.
 * Search volume is deliberately bounded through provider configuration.
 */
@Component
public class MicrosoftCareersProvider implements JobProvider {

    static final String DEFAULT_BASE_URL = "https://apply.careers.microsoft.com";
    static final String ALLOWED_HOST = "apply.careers.microsoft.com";
    static final int PAGE_SIZE = 10;
    static final int DEFAULT_MAX_RESULTS = 100;
    static final int ABSOLUTE_MAX_RESULTS = 500;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MicrosoftCareersProvider(RestClient.Builder atsRestClientBuilder, ObjectMapper objectMapper) {
        this.restClient = atsRestClientBuilder
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("User-Agent", "CareerOS/1.0")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.MICROSOFT_CAREERS;
    }

    @Override
    public List<JobPostingCommand> fetchJobs(Company company) {
        Configuration configuration = configuration(company);
        List<JobPostingCommand> jobs = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int start = 0; start < configuration.maxResults(); start += PAGE_SIZE) {
            SearchResponse response = requestPage(configuration, start);
            List<Position> positions = response == null || response.data() == null
                    ? List.of() : response.data().positions();
            if (positions == null || positions.isEmpty()) {
                break;
            }
            for (Position position : positions) {
                if (jobs.size() >= configuration.maxResults()) {
                    break;
                }
                JobPostingCommand command = toCommand(company, configuration.baseUrl(), position);
                if (command != null && seen.add(command.externalId())) {
                    jobs.add(command);
                }
            }
            if (positions.size() < PAGE_SIZE) {
                break;
            }
        }
        return jobs;
    }

    private SearchResponse requestPage(Configuration configuration, int start) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return restClient.get()
                        .uri(builder -> builder
                                .scheme("https")
                                .host(ALLOWED_HOST)
                                .path("/api/pcsx/search")
                                .queryParam("domain", configuration.domain())
                                .queryParam("query", configuration.query())
                                .queryParam("location", configuration.location())
                                .queryParam("start", start)
                                .queryParam("num", PAGE_SIZE)
                                .queryParam("sort_by", "timestamp")
                                .build())
                        .retrieve()
                        .body(SearchResponse.class);
            } catch (RuntimeException failure) {
                lastFailure = failure;
                if (attempt < 3) {
                    try {
                        Thread.sleep(100L * (1L << (attempt - 1)));
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Microsoft careers synchronization interrupted", interrupted);
                    }
                }
            }
        }
        throw new IllegalStateException("Microsoft careers search failed after 3 attempts", lastFailure);
    }

    private Configuration configuration(Company company) {
        String baseUrl = DEFAULT_BASE_URL;
        String domain = "microsoft.com";
        String query = "Senior Software Engineer";
        String location = "United States";
        int maxResults = DEFAULT_MAX_RESULTS;
        try {
            if (company.getProviderConfiguration() != null && !company.getProviderConfiguration().isBlank()) {
                JsonNode node = objectMapper.readTree(company.getProviderConfiguration());
                baseUrl = text(node, "baseUrl", baseUrl);
                domain = text(node, "domain", domain);
                query = text(node, "query", query);
                location = text(node, "location", location);
                maxResults = node.path("maxResults").asInt(maxResults);
            }
            URI uri = URI.create(baseUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !ALLOWED_HOST.equalsIgnoreCase(uri.getHost())) {
                throw new IllegalArgumentException("baseUrl must be https://" + ALLOWED_HOST);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid Microsoft careers provider configuration", exception);
        }
        return new Configuration(baseUrl, domain, query, location,
                Math.max(PAGE_SIZE, Math.min(maxResults, ABSOLUTE_MAX_RESULTS)));
    }

    private static String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText();
        return value == null || value.isBlank() ? fallback : value;
    }

    private static JobPostingCommand toCommand(Company company, String baseUrl, Position position) {
        if (position == null || position.id() == null || position.name() == null || position.name().isBlank()) {
            return null;
        }
        String externalId = String.valueOf(position.id());
        String location = position.locations() == null ? null : String.join("; ", position.locations());
        String applyUrl = position.positionUrl() == null || position.positionUrl().isBlank()
                ? baseUrl + "/careers/job/" + externalId
                : URI.create(baseUrl).resolve(position.positionUrl()).toString();
        boolean remote = "remote".equalsIgnoreCase(position.workLocationOption())
                || RemoteHeuristic.looksRemote(position.name(), location, position.locationFlexibility());
        LocalDate postedDate = position.postedTs() == null ? null
                : Instant.ofEpochSecond(position.postedTs()).atZone(ZoneOffset.UTC).toLocalDate();
        String description = joinMetadata(position.department(), position.businessUnit());

        return new JobPostingCommand(externalId, company.getId(), position.name(), location,
                EmploymentTypeParser.fromKeyword(position.employmentType()), remote,
                null, null, null, description, postedDate, applyUrl);
    }

    private static String joinMetadata(String department, String businessUnit) {
        List<String> values = new ArrayList<>();
        if (department != null && !department.isBlank()) values.add("Department: " + department);
        if (businessUnit != null && !businessUnit.isBlank()) values.add("Business unit: " + businessUnit);
        return values.isEmpty() ? null : String.join("\n", values);
    }

    private record Configuration(String baseUrl, String domain, String query, String location, int maxResults) {
    }

    private record SearchResponse(SearchData data) {
    }

    private record SearchData(Integer count, List<Position> positions) {
    }

    private record Position(Long id, String displayJobId, String name, List<String> locations,
                            Long postedTs, String department, String businessUnit,
                            String workLocationOption, String locationFlexibility,
                            String employmentType, String positionUrl) {
    }
}
