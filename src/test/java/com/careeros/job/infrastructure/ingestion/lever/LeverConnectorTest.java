package com.careeros.job.infrastructure.ingestion.lever;

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

class LeverConnectorTest {

    private static final String BASE_URL = "https://api.lever.co/v0/postings";

    private static IngestionProperties properties() {
        return new IngestionProperties(false, Duration.ofMinutes(30), Duration.ofSeconds(5), Duration.ofSeconds(10),
                "https://boards-api.greenhouse.io/v1/boards", BASE_URL,
                "https://api.ashbyhq.com/posting-api/job-board", "https://api.smartrecruiters.com/v1/companies",
                "myworkdayjobs.com");
    }

    private static Company aCompany() {
        return Company.create("Acme Inc", "https://acme.example/careers", AtsType.LEVER, Priority.HIGH, true, "acme");
    }

    @Test
    void mapsLeverPostingsToCommands() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/acme?mode=json"))
                .andRespond(withSuccess(TestFixtures.read("fixtures/ingestion/lever/sample-response.json"),
                        MediaType.APPLICATION_JSON));

        LeverConnector connector = new LeverConnector(builder, properties());
        List<JobPostingCommand> commands = connector.fetchJobs(aCompany());

        assertThat(commands).hasSize(2);
        JobPostingCommand remoteJob = commands.get(0);
        assertThat(remoteJob.externalId()).isEqualTo("abc-123");
        assertThat(remoteJob.title()).isEqualTo("Staff Software Engineer");
        assertThat(remoteJob.location()).isEqualTo("New York, NY");
        assertThat(remoteJob.remote()).isTrue();
        assertThat(remoteJob.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(remoteJob.postedDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(remoteJob.applyUrl()).isEqualTo("https://jobs.lever.co/acme/abc-123/apply");

        JobPostingCommand onsiteJob = commands.get(1);
        assertThat(onsiteJob.remote()).isFalse();
        assertThat(onsiteJob.employmentType()).isEqualTo(EmploymentType.CONTRACT);
        // applyUrl falls back to hostedUrl when applyUrl is absent
        assertThat(onsiteJob.applyUrl()).isEqualTo("https://jobs.lever.co/acme/def-456");

        server.verify();
    }

    @Test
    void propagatesExceptionOnUpstreamServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/acme?mode=json"))
                .andRespond(withServerError());

        LeverConnector connector = new LeverConnector(builder, properties());

        assertThatThrownBy(() -> connector.fetchJobs(aCompany()))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
