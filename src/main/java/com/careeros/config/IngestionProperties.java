package com.careeros.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration for ATS ingestion: whether the scheduled poller runs, how
 * often, HTTP timeouts, and the base URL of each ATS's public job-board API.
 */
@ConfigurationProperties(prefix = "careeros.ingestion")
public record IngestionProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("30m") Duration pollInterval,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout,
        @DefaultValue("https://boards-api.greenhouse.io/v1/boards") String greenhouseBaseUrl,
        @DefaultValue("https://api.lever.co/v0/postings") String leverBaseUrl,
        @DefaultValue("https://api.ashbyhq.com/posting-api/job-board") String ashbyBaseUrl,
        @DefaultValue("https://api.smartrecruiters.com/v1/companies") String smartRecruitersBaseUrl,
        @DefaultValue("myworkdayjobs.com") String workdayHostSuffix
) {
}
