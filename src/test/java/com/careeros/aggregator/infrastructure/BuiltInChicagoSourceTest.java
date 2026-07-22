package com.careeros.aggregator.infrastructure;

import com.careeros.job.domain.EmploymentType;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuiltInChicagoSourceTest {
    private final BuiltInChicagoSource source = new BuiltInChicagoSource(RestClient.builder(),
            new BuiltInChicagoProperties(true, "https://www.builtinchicago.org",
                    "/jobs/dev-engineering/senior", 25));

    @Test
    void parsesJobsFromSingleServerRenderedListingWithoutDetailRequests() {
        var listing = Jsoup.parse("""
                <div id="job-card-9891312" data-id="job-card">
                  <a href="/company/comcast" data-id="company-title"><span>Comcast</span></a>
                  <a href="/job/backend-engineer/9891312" data-id="job-card-title"
                     data-builtin-track-job-id="9891312">Senior Backend Engineer</a>
                  <div><div><i class="fa-regular fa-clock"></i></div><span class="font-barlow text-gray-04">Reposted 2 Days Ago</span></div>
                  <div><div><i class="fa-regular fa-house-building"></i></div><span class="font-barlow text-gray-04">Remote</span></div>
                  <div><div><i class="fa-regular fa-location-dot"></i></div><span class="font-barlow text-gray-04">Chicago, IL</span></div>
                  <div><div><i class="fa-regular fa-sack-dollar"></i></div><span class="font-barlow text-gray-04">110K-165K Annually</span></div>
                  <div id="drop-data-9891312"><div class="fs-sm fw-regular mb-md text-gray-04">Build Java and Kafka services.</div></div>
                </div>
                """, "https://www.builtinchicago.org/jobs/dev-engineering/senior");

        var jobs = source.parseListing(listing);

        assertThat(jobs).hasSize(1);
        var job = jobs.getFirst();
        assertThat(job.externalId()).isEqualTo("9891312");
        assertThat(job.companyName()).isEqualTo("Comcast");
        assertThat(job.companyUrl()).isEqualTo("https://www.builtinchicago.org/company/comcast");
        assertThat(job.title()).isEqualTo("Senior Backend Engineer");
        assertThat(job.location()).isEqualTo("Chicago, IL");
        assertThat(job.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(job.remote()).isTrue();
        assertThat(job.salaryMin()).isEqualByComparingTo(new BigDecimal("110000"));
        assertThat(job.salaryMax()).isEqualByComparingTo(new BigDecimal("165000"));
        assertThat(job.salaryCurrency()).isEqualTo("USD");
        assertThat(job.description()).isEqualTo("Build Java and Kafka services.");
        assertThat(job.postedDate()).isEqualTo(LocalDate.now().minusDays(2));
        assertThat(job.applyUrl()).isEqualTo("https://www.builtinchicago.org/job/backend-engineer/9891312");
        assertThat(job.sourceUrl()).isEqualTo(job.applyUrl());
    }

    @Test
    void reportsUpstreamMarkupChangesClearly() {
        assertThatThrownBy(() -> source.parseListing(Jsoup.parse("<html></html>")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no recognizable job cards");
    }
}
