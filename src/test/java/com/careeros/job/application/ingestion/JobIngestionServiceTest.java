package com.careeros.job.application.ingestion;

import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.company.domain.Priority;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.application.JobPostingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobIngestionServiceTest {

    @Mock
    private JobPostingService jobPostingService;

    @Mock
    private CompanyRepository companyRepository;

    private static Company aCompany(String name, AtsType atsType) {
        return Company.create(name, "https://" + name + ".example/careers", atsType, Priority.HIGH, true, "id-" + name);
    }

    private static JobPostingCommand aCommand(String externalId) {
        return new JobPostingCommand(externalId, UUID.randomUUID(), "Engineer", "Remote", null, true,
                null, null, null, null, LocalDate.now(), "https://example/apply/" + externalId);
    }

    private static class FakeConnector implements AtsConnector {
        private final AtsType atsType;
        private final List<JobPostingCommand> commands;
        private final RuntimeException failure;

        FakeConnector(AtsType atsType, List<JobPostingCommand> commands) {
            this.atsType = atsType;
            this.commands = commands;
            this.failure = null;
        }

        FakeConnector(AtsType atsType, RuntimeException failure) {
            this.atsType = atsType;
            this.commands = null;
            this.failure = failure;
        }

        @Override
        public AtsType supportedType() {
            return atsType;
        }

        @Override
        public List<JobPostingCommand> fetchJobs(Company company) {
            if (failure != null) {
                throw failure;
            }
            return commands;
        }
    }

    @Test
    void duplicatePostingsAreSkippedNotFailed() {
        Company company = aCompany("acme", AtsType.GREENHOUSE);
        when(companyRepository.findAllEnabled()).thenReturn(List.of(company));
        when(jobPostingService.synchronize(any()))
                .thenReturn(JobPostingService.SynchronizationOutcome.UNCHANGED);

        JobIngestionService service = new JobIngestionService(jobPostingService, companyRepository,
                List.of(new FakeConnector(AtsType.GREENHOUSE, List.of(aCommand("job-1")))));

        IngestionSummary summary = service.ingestAllEnabledCompanies();

        assertThat(summary.totalCreated()).isZero();
        assertThat(summary.totalSkipped()).isEqualTo(1);
        assertThat(summary.totalFailed()).isZero();
    }

    @Test
    void unexpectedPersistFailureIsCountedAndLoopContinues() {
        Company company = aCompany("acme", AtsType.GREENHOUSE);
        when(companyRepository.findAllEnabled()).thenReturn(List.of(company));
        when(jobPostingService.synchronize(any()))
                .thenThrow(new IllegalArgumentException("bad data"))
                .thenReturn(JobPostingService.SynchronizationOutcome.CREATED);

        JobIngestionService service = new JobIngestionService(jobPostingService, companyRepository,
                List.of(new FakeConnector(AtsType.GREENHOUSE, List.of(aCommand("job-1"), aCommand("job-2")))));

        IngestionSummary summary = service.ingestAllEnabledCompanies();

        assertThat(summary.totalCreated()).isEqualTo(1);
        assertThat(summary.totalFailed()).isEqualTo(1);
        assertThat(summary.results().get(0).errors()).hasSize(1);
    }

    @Test
    void oneCompanysFetchFailureDoesNotAffectOthers() {
        Company failingCompany = aCompany("broken", AtsType.GREENHOUSE);
        Company healthyCompany = aCompany("healthy", AtsType.LEVER);
        when(companyRepository.findAllEnabled()).thenReturn(List.of(failingCompany, healthyCompany));
        when(jobPostingService.synchronize(any())).thenReturn(JobPostingService.SynchronizationOutcome.CREATED);

        JobIngestionService service = new JobIngestionService(jobPostingService, companyRepository, List.of(
                new FakeConnector(AtsType.GREENHOUSE, new RuntimeException("network error")),
                new FakeConnector(AtsType.LEVER, List.of(aCommand("job-1")))));

        IngestionSummary summary = service.ingestAllEnabledCompanies();

        assertThat(summary.companiesProcessed()).isEqualTo(2);
        CompanyIngestionResult failingResult = summary.results().stream()
                .filter(r -> r.companyName().equals(failingCompany.getName())).findFirst().orElseThrow();
        assertThat(failingResult.errors()).anyMatch(e -> e.contains("fetch failed"));

        CompanyIngestionResult healthyResult = summary.results().stream()
                .filter(r -> r.companyName().equals(healthyCompany.getName())).findFirst().orElseThrow();
        assertThat(healthyResult.created()).isEqualTo(1);
    }

    @Test
    void companyWithUnsupportedAtsTypeIsHandledGracefully() {
        Company company = aCompany("acme", AtsType.OTHER);
        when(companyRepository.findAllEnabled()).thenReturn(List.of(company));

        JobIngestionService service = new JobIngestionService(jobPostingService, companyRepository,
                List.of(new FakeConnector(AtsType.GREENHOUSE, List.of())));

        IngestionSummary summary = service.ingestAllEnabledCompanies();

        assertThat(summary.results()).hasSize(1);
        assertThat(summary.results().get(0).errors()).anyMatch(e -> e.contains("no connector registered"));
    }

    @Test
    void duplicateConnectorRegistrationFailsFastAtConstruction() {
        assertThatThrownBy(() -> new JobIngestionService(jobPostingService, companyRepository, List.of(
                new FakeConnector(AtsType.GREENHOUSE, List.of()),
                new FakeConnector(AtsType.GREENHOUSE, List.of()))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ingestingUnknownCompanyIdThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        JobIngestionService service = new JobIngestionService(jobPostingService, companyRepository, List.of());

        assertThatThrownBy(() -> service.ingestCompany(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
