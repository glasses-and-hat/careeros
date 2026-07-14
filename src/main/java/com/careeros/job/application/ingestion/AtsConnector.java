package com.careeros.job.application.ingestion;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.job.application.JobPostingCommand;

import java.util.List;

/**
 * Port for fetching normalized job postings from a specific ATS's public
 * job-board API. Implementations are inbound adapters living under
 * {@code job.infrastructure.ingestion.<ats>} — one per {@link AtsType}.
 *
 * <p>Implementations must let exceptions (HTTP errors, malformed responses)
 * propagate rather than swallowing them; {@link JobIngestionService} is the
 * single place that catches and isolates per-company fetch failures so that
 * error handling isn't duplicated across every connector.
 */
public interface AtsConnector {

    AtsType supportedType();

    List<JobPostingCommand> fetchJobs(Company company);
}
