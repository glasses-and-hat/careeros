package com.careeros.aggregator.application;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.company.domain.Priority;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.JobPostingService;
import com.careeros.job.domain.JobSourceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile({"local", "docker"})
@ConditionalOnProperty(prefix = "careeros.aggregators.builtin-chicago", name = "enabled", havingValue = "true")
public class AggregatorIngestionService {

    private final AggregatorJobSource source;
    private final CompanyRepository companies;
    private final JobPostingService jobs;

    public AggregatorIngestionService(AggregatorJobSource source, CompanyRepository companies,
                                      JobPostingService jobs) {
        this.source = source;
        this.companies = companies;
        this.jobs = jobs;
    }

    public AggregatorRunResult run() {
        var discovered = source.discoverJobs();
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        int companiesCreated = 0;
        var errors = new ArrayList<String>();

        for (var job : discovered) {
            try {
                if (jobs.existsByApplyUrl(job.applyUrl())) {
                    skipped++;
                    continue;
                }
                var resolution = resolveCompany(job);
                if (resolution.created()) companiesCreated++;
                var command = new JobPostingCommand(
                        "builtin-chicago:" + job.externalId(), resolution.company().getId(), job.title(),
                        job.location(), job.employmentType(), job.remote(), job.salaryMin(), job.salaryMax(),
                        job.salaryCurrency(), job.description(), job.postedDate(), job.applyUrl(),
                        JobSourceType.AGGREGATOR, job.sourceUrl());
                switch (jobs.synchronize(command)) {
                    case CREATED -> created++;
                    case UPDATED -> updated++;
                    case UNCHANGED -> skipped++;
                }
            } catch (Exception exception) {
                failed++;
                errors.add(job.externalId() + ": " + exception.getMessage());
            }
        }
        return new AggregatorRunResult(source.sourceName(), discovered.size(), created, updated, skipped,
                failed, companiesCreated, List.copyOf(errors));
    }

    private CompanyResolution resolveCompany(AggregatedJob job) {
        return companies.findByNameIgnoreCase(job.companyName())
                .map(company -> new CompanyResolution(company, false))
                .orElseGet(() -> {
                    var company = Company.create(job.companyName(), job.companyUrl(), AtsType.OTHER,
                            Priority.MEDIUM, false, null);
                    return new CompanyResolution(companies.save(company), true);
                });
    }

    private record CompanyResolution(Company company, boolean created) {
    }
}
