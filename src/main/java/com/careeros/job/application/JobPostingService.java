package com.careeros.job.application;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.JobPostingFilter;
import com.careeros.job.domain.JobPostingRepository;
import com.careeros.job.domain.SalaryRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use cases for managing normalized job postings. Duplicate detection at
 * this stage is limited to the content hash computed by {@link JobPosting};
 * semantic/fuzzy duplicate detection across ATS re-postings is future work.
 */
@Service
@Transactional
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository, CompanyRepository companyRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.companyRepository = companyRepository;
    }

    public JobPosting create(JobPostingCommand command) {
        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Company", command.companyId()));

        JobPosting jobPosting = JobPosting.create(
                command.externalId(),
                company,
                command.title(),
                command.location(),
                command.employmentType(),
                command.remote(),
                new SalaryRange(command.salaryMin(), command.salaryMax(), command.salaryCurrency()),
                command.description(),
                command.postedDate(),
                command.applyUrl());

        if (jobPostingRepository.existsByHash(jobPosting.getHash())) {
            throw new DuplicateResourceException(
                    "Job posting '%s' from company '%s' already exists".formatted(command.externalId(), company.getName()));
        }
        return jobPostingRepository.save(jobPosting);
    }

    public JobPosting update(UUID id, JobPostingCommand command) {
        JobPosting jobPosting = getOrThrow(id);
        jobPosting.updateDetails(
                command.title(),
                command.location(),
                command.employmentType(),
                command.remote(),
                new SalaryRange(command.salaryMin(), command.salaryMax(), command.salaryCurrency()),
                command.description(),
                command.postedDate(),
                command.applyUrl());
        return jobPostingRepository.save(jobPosting);
    }

    @Transactional(readOnly = true)
    public JobPosting get(UUID id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Page<JobPosting> list(JobPostingFilter filter, Pageable pageable) {
        return jobPostingRepository.findAll(filter, pageable);
    }

    public void delete(UUID id) {
        if (!jobPostingRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("JobPosting", id);
        }
        jobPostingRepository.deleteById(id);
    }

    private JobPosting getOrThrow(UUID id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("JobPosting", id));
    }
}
