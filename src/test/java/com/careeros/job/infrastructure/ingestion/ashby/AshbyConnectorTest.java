package com.careeros.job.infrastructure.ingestion.ashby;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.Priority;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.domain.EmploymentType;
import com.careeros.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AshbyConnectorTest {

    private static final String BASE_URL = "https://api.ashbyhq.com/posting-api/job-board";

    private static IngestionProperties properties() {
        return new IngestionProperties(false, Duration.ofMinutes(30), Duration.ofSeconds(5), Duration.ofSeconds(10),
                "https://boards-api.greenhouse.io/v1/boards", "https://api.lever.co/v0/postings", BASE_URL,
                "https://api.smartrecruiters.com/v1/companies", "myworkdayjobs.com");
    }

    private static Company aCompany() {
        return Company.create("Acme Inc", "https://acme.example/careers", AtsType.ASHBY, Priority.HIGH, true, "acme");
    }

    @Test
    void mapsAshbyJobsToCommands() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/acme"))
                .andRespond(withSuccess(TestFixtures.read("fixtures/ingestion/ashby/sample-response.json"),
                        MediaType.APPLICATION_JSON));

        AshbyConnector connector = new AshbyConnector(builder, properties());
        List<JobPostingCommand> commands = connector.fetchJobs(aCompany());

        assertThat(commands).hasSize(2);
        JobPostingCommand remoteJob = commands.get(0);
        assertThat(remoteJob.externalId()).isEqualTo("job-001");
        assertThat(remoteJob.title()).isEqualTo("Engineering Manager");
        assertThat(remoteJob.remote()).isTrue();
        assertThat(remoteJob.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(remoteJob.postedDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(remoteJob.applyUrl()).isEqualTo("https://jobs.ashbyhq.com/acme/job-001/apply");

        JobPostingCommand internJob = commands.get(1);
        assertThat(internJob.remote()).isFalse();
        assertThat(internJob.employmentType()).isEqualTo(EmploymentType.INTERNSHIP);
        // applyUrl falls back to jobUrl when applyUrl is absent
        assertThat(internJob.applyUrl()).isEqualTo("https://jobs.ashbyhq.com/acme/job-002");

        server.verify();
    }

    @Test
    void propagatesExceptionOnUpstreamServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/acme"))
                .andRespond(withServerError());

        AshbyConnector connector = new AshbyConnector(builder, properties());

        assertThatThrownBy(() -> connector.fetchJobs(aCompany()))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
