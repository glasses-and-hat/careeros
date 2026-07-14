package com.careeros.job.infrastructure.ingestion.smartrecruiters;

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

class SmartRecruitersConnectorTest {

    private static final String BASE_URL = "https://api.smartrecruiters.com/v1/companies";

    private static IngestionProperties properties() {
        return new IngestionProperties(false, Duration.ofMinutes(30), Duration.ofSeconds(5), Duration.ofSeconds(10),
                "https://boards-api.greenhouse.io/v1/boards", "https://api.lever.co/v0/postings",
                "https://api.ashbyhq.com/posting-api/job-board", BASE_URL, "myworkdayjobs.com");
    }

    private static Company aCompany() {
        return Company.create("Acme Inc", "https://acme.example/careers", AtsType.SMARTRECRUITERS, Priority.HIGH,
                true, "AcmeInc");
    }

    @Test
    void mapsSmartRecruitersPostingsToCommandsAndStopsAtTotalFound() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/AcmeInc/postings?offset=0&limit=100"))
                .andRespond(withSuccess(TestFixtures.read("fixtures/ingestion/smartrecruiters/sample-response.json"),
                        MediaType.APPLICATION_JSON));

        SmartRecruitersConnector connector = new SmartRecruitersConnector(builder, properties());
        List<JobPostingCommand> commands = connector.fetchJobs(aCompany());

        // totalFound=2 <= PAGE_SIZE, so only one page is fetched
        assertThat(commands).hasSize(2);
        JobPostingCommand remoteJob = commands.get(0);
        assertThat(remoteJob.externalId()).isEqualTo("posting-1");
        assertThat(remoteJob.location()).isEqualTo("Berlin, Germany");
        assertThat(remoteJob.remote()).isTrue();
        assertThat(remoteJob.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(remoteJob.postedDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(remoteJob.applyUrl()).isEqualTo("https://jobs.smartrecruiters.com/AcmeInc/posting-1");

        JobPostingCommand onsiteJob = commands.get(1);
        assertThat(onsiteJob.location()).isEqualTo("Chicago, IL, USA");
        assertThat(onsiteJob.remote()).isFalse();
        assertThat(onsiteJob.employmentType()).isEqualTo(EmploymentType.PART_TIME);
        // ref is null, applyUrl falls back to a constructed jobs.smartrecruiters.com URL
        assertThat(onsiteJob.applyUrl()).isEqualTo("https://jobs.smartrecruiters.com/AcmeInc/posting-2");

        server.verify();
    }

    @Test
    void propagatesExceptionOnUpstreamServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/AcmeInc/postings?offset=0&limit=100"))
                .andRespond(withServerError());

        SmartRecruitersConnector connector = new SmartRecruitersConnector(builder, properties());

        assertThatThrownBy(() -> connector.fetchJobs(aCompany()))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
