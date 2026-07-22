package com.careeros.provider.infrastructure.microsoft;

import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.Priority;
import com.careeros.provider.domain.ProviderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MicrosoftCareersProviderTest {

    @Test
    void discoversAndNormalizesMicrosoftSearchResults() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://apply.careers.microsoft.com/api/pcsx/search"
                        + "?domain=microsoft.com&query=Senior%20Software%20Engineer&location=United%20States"
                        + "&start=0&num=10&sort_by=timestamp"))
                .andExpect(queryParam("domain", "microsoft.com"))
                .andRespond(withSuccess("""
                        {"data":{"count":1,"positions":[{
                          "id":1970393556929429,
                          "displayJobId":"200043372",
                          "name":"Senior Software Engineer - Azure Compute",
                          "locations":["United States, Multiple Locations, Multiple Locations"],
                          "postedTs":1784676140,
                          "department":"Software Engineering",
                          "workLocationOption":"remote",
                          "positionUrl":"/careers/job/1970393556929429"
                        }]}}
                        """, MediaType.APPLICATION_JSON));

        Company company = Company.create("Microsoft", "https://apply.careers.microsoft.com/careers",
                AtsType.OTHER, Priority.HIGH, true, null);
        company.configureProvider(ProviderType.MICROSOFT_CAREERS,
                "{\"query\":\"Senior Software Engineer\",\"location\":\"United States\",\"maxResults\":10}",
                List.of());

        var jobs = new MicrosoftCareersProvider(builder, new ObjectMapper()).fetchJobs(company);

        assertThat(jobs).singleElement().satisfies(job -> {
            assertThat(job.externalId()).isEqualTo("1970393556929429");
            assertThat(job.title()).isEqualTo("Senior Software Engineer - Azure Compute");
            assertThat(job.location()).contains("United States");
            assertThat(job.remote()).isTrue();
            assertThat(job.postedDate()).isEqualTo(LocalDate.of(2026, 7, 21));
            assertThat(job.applyUrl()).isEqualTo(
                    "https://apply.careers.microsoft.com/careers/job/1970393556929429");
            assertThat(job.description()).contains("Department: Software Engineering");
        });
        server.verify();
    }
}
