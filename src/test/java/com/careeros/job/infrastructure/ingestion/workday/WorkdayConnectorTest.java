package com.careeros.job.infrastructure.ingestion.workday;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.Priority;
import com.careeros.config.IngestionProperties;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WorkdayConnectorTest {

    private static final String JOBS_URL = "https://acme.myworkdayjobs.com/wday/cxs/acme/External/jobs";

    private static IngestionProperties properties() {
        return new IngestionProperties(false, Duration.ofMinutes(30), Duration.ofSeconds(5), Duration.ofSeconds(10),
                "https://boards-api.greenhouse.io/v1/boards", "https://api.lever.co/v0/postings",
                "https://api.ashbyhq.com/posting-api/job-board", "https://api.smartrecruiters.com/v1/companies",
                "myworkdayjobs.com");
    }

    private static Company aCompany() {
        return Company.create("Acme Inc", "https://acme.example/careers", AtsType.WORKDAY, Priority.HIGH, true,
                "acme/External");
    }

    @Test
    void mapsWorkdayJobPostingsToCommands() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(JOBS_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(TestFixtures.read("fixtures/ingestion/workday/sample-response.json"),
                        MediaType.APPLICATION_JSON));

        WorkdayConnector connector = new WorkdayConnector(builder, properties());
        List<JobPostingCommand> commands = connector.fetchJobs(aCompany());

        assertThat(commands).hasSize(2);
        JobPostingCommand withRequisition = commands.get(0);
        assertThat(withRequisition.externalId()).isEqualTo("REQ-1234");
        assertThat(withRequisition.title()).isEqualTo("Senior Financial Analyst");
        assertThat(withRequisition.location()).isEqualTo("Remote - USA");
        assertThat(withRequisition.remote()).isTrue();
        // postedOn is a relative string ("Posted Today") — deliberately not parsed
        assertThat(withRequisition.postedDate()).isNull();
        assertThat(withRequisition.applyUrl())
                .isEqualTo("https://acme.myworkdayjobs.com/en-US/External/job/Remote/Senior-Financial-Analyst_REQ-1234");

        JobPostingCommand withoutBulletFields = commands.get(1);
        // no bulletFields, falls back to the last externalPath segment
        assertThat(withoutBulletFields.externalId()).isEqualTo("Warehouse-Associate_REQ-5678");
        assertThat(withoutBulletFields.remote()).isFalse();

        server.verify();
    }

    @Test
    void propagatesExceptionOnUpstreamServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(JOBS_URL))
                .andRespond(withServerError());

        WorkdayConnector connector = new WorkdayConnector(builder, properties());

        assertThatThrownBy(() -> connector.fetchJobs(aCompany()))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
