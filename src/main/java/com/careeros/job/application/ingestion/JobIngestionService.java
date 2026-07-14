package com.careeros.job.application.ingestion;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.JobPostingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates ATS ingestion: for each enabled company, dispatches to the
 * {@link AtsConnector} matching its {@link AtsType}, then persists each
 * fetched posting via {@link JobPostingService#create}.
 *
 * <p><b>Deliberately not {@code @Transactional}.</b> {@link JobPostingService#create}
 * is already transactional per call. If this class were also transactional,
 * every {@code create()} in a run would join the same physical transaction,
 * and Spring's AOP interceptor marks that transaction rollback-only on any
 * unchecked exception raised by a participating method — including
 * {@link DuplicateResourceException}, even though it's caught immediately
 * below. The first duplicate in a run would silently poison the whole batch,
 * surfacing only as {@code UnexpectedRollbackException} at commit time.
 * Staying non-transactional keeps each posting's {@code create()} call an
 * independent top-level transaction, so one posting's outcome can never
 * affect another's.
 */
@Service
public class JobIngestionService {

    private static final Logger log = LoggerFactory.getLogger(JobIngestionService.class);

    private final JobPostingService jobPostingService;
    private final CompanyRepository companyRepository;
    private final Map<AtsType, AtsConnector> connectorsByType;

    public JobIngestionService(JobPostingService jobPostingService, CompanyRepository companyRepository,
                                List<AtsConnector> connectors) {
        this.jobPostingService = jobPostingService;
        this.companyRepository = companyRepository;
        this.connectorsByType = connectors.stream()
                .collect(Collectors.toMap(AtsConnector::supportedType, Function.identity()));
    }

    public IngestionSummary ingestAllEnabledCompanies() {
        return IngestionSummary.of(companyRepository.findAllEnabled().stream()
                .map(this::ingestOneCompany)
                .toList());
    }

    public IngestionSummary ingestCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Company", companyId));
        return IngestionSummary.of(List.of(ingestOneCompany(company)));
    }

    private CompanyIngestionResult ingestOneCompany(Company company) {
        AtsConnector connector = connectorsByType.get(company.getAtsType());
        if (connector == null) {
            return CompanyIngestionResult.unsupportedAtsType(company);
        }

        List<JobPostingCommand> commands;
        try {
            commands = connector.fetchJobs(company);
        } catch (Exception e) {
            log.warn("Ingestion fetch failed for company {} ({}): {}", company.getId(), company.getAtsType(),
                    e.getMessage());
            return CompanyIngestionResult.fetchFailed(company, e.getMessage());
        }

        int created = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        for (JobPostingCommand command : commands) {
            try {
                jobPostingService.create(command);
                created++;
            } catch (DuplicateResourceException e) {
                skipped++;
            } catch (Exception e) {
                failed++;
                errors.add(command.externalId() + ": " + e.getMessage());
                log.warn("Failed to persist job posting {} for company {}: {}", command.externalId(),
                        company.getId(), e.getMessage());
            }
        }
        return new CompanyIngestionResult(company.getId(), company.getName(), company.getAtsType(), created,
                skipped, failed, errors);
    }
}
