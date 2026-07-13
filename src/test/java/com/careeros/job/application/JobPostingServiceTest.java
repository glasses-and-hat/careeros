package com.careeros.job.application;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.company.domain.Priority;
import com.careeros.job.domain.EmploymentType;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private CompanyRepository companyRepository;

    private JobPostingService jobPostingService;
    private Company company;

    @BeforeEach
    void setUp() {
        jobPostingService = new JobPostingService(jobPostingRepository, companyRepository);
        company = Company.create("Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);
    }

    private static JobPostingCommand aCommand(UUID companyId) {
        return new JobPostingCommand(
                "job-123", companyId, "Senior Backend Engineer", "Remote", EmploymentType.FULL_TIME, true,
                null, null, null, "Great job", LocalDate.now(), "https://acme.example/apply/job-123");
    }

    @Test
    void createsJobPostingWhenCompanyExistsAndNotDuplicate() {
        UUID companyId = UUID.randomUUID();
        JobPostingCommand command = aCommand(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(jobPostingRepository.existsByHash(any())).thenReturn(false);
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting result = jobPostingService.create(command);

        assertThat(result.getTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(result.getCompany()).isEqualTo(company);
        verify(jobPostingRepository).save(any(JobPosting.class));
    }

    @Test
    void throwsWhenCompanyDoesNotExist() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingService.create(aCommand(companyId)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateJobPostingByHash() {
        UUID companyId = UUID.randomUUID();
        JobPostingCommand command = aCommand(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(jobPostingRepository.existsByHash(any())).thenReturn(true);

        assertThatThrownBy(() -> jobPostingService.create(command))
                .isInstanceOf(DuplicateResourceException.class);
        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    void throwsWhenJobPostingNotFoundOnGet() {
        UUID id = UUID.randomUUID();
        when(jobPostingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
