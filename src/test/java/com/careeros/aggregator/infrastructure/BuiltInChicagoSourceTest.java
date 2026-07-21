package com.careeros.aggregator.infrastructure;

import com.careeros.job.domain.EmploymentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltInChicagoSourceTest {

    private final BuiltInChicagoSource source = new BuiltInChicagoSource(
            RestClient.builder(), new ObjectMapper(),
            new BuiltInChicagoProperties(true, "https://www.builtinchicago.org",
                    "/jobs/dev-engineering/senior", 25, Duration.ZERO));

    @Test
    void parsesStructuredEmployerJobDataAndCanonicalApplicationUrl() {
        var document = Jsoup.parse("""
                <html><head><title>Senior Backend Engineer - New Relic</title>
                <script type="application/ld+json">
                {"@graph":[{"@type":"JobPosting","title":"Senior Backend Engineer",
                "description":"<p>Build fully remote Java and Kafka services. #Remote</p>",
                "identifier":{"value":"9885470"},"datePosted":"2026-07-21",
                "employmentType":"FULL_TIME",
                "hiringOrganization":{"name":"New Relic","sameAs":"https://builtin.com/company/new-relic"},
                "jobLocation":{"address":{"addressLocality":"Chicago","addressRegion":"Illinois","addressCountry":"USA"}},
                "baseSalary":{"currency":"USD","value":{"minValue":156000,"maxValue":195000}}}]}
                </script></head><body>
                <script>Builtin.jobPostInit({"job":{"howToApply":"https://job-boards.greenhouse.io/newrelic/jobs/5255769008?source=builtin"}});</script>
                </body></html>
                """, "https://www.builtinchicago.org/job/senior-backend-engineer/9885470");

        var job = source.parseDetail(document,
                "https://www.builtinchicago.org/job/senior-backend-engineer/9885470");

        assertThat(job.externalId()).isEqualTo("9885470");
        assertThat(job.companyName()).isEqualTo("New Relic");
        assertThat(job.title()).isEqualTo("Senior Backend Engineer");
        assertThat(job.location()).isEqualTo("Chicago, Illinois, USA");
        assertThat(job.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(job.remote()).isTrue();
        assertThat(job.salaryMin()).isEqualByComparingTo(new BigDecimal("156000"));
        assertThat(job.salaryMax()).isEqualByComparingTo(new BigDecimal("195000"));
        assertThat(job.salaryCurrency()).isEqualTo("USD");
        assertThat(job.postedDate()).isEqualTo(LocalDate.of(2026, 7, 21));
        assertThat(job.applyUrl()).isEqualTo("https://job-boards.greenhouse.io/newrelic/jobs/5255769008");
        assertThat(job.sourceUrl()).contains("builtinchicago.org/job/");
    }

    @Test
    void fallsBackToAttributedSourceUrlWhenDirectApplicationLinkIsUnavailable() {
        var document = Jsoup.parse("""
                <script type="application/ld+json">
                {"@type":"JobPosting","title":"Platform Engineer","description":"Build systems",
                "identifier":{"value":"42"},"employmentType":"CONTRACTOR",
                "hiringOrganization":{"name":"Local Startup"},
                "jobLocation":[{"address":{"addressLocality":"Chicago","addressRegion":"IL","addressCountry":"USA"}}]}
                </script>
                """);
        var url = "https://www.builtinchicago.org/job/platform-engineer/42";

        var job = source.parseDetail(document, url);

        assertThat(job.companyUrl()).isEqualTo(url);
        assertThat(job.applyUrl()).isEqualTo(url);
        assertThat(job.employmentType()).isEqualTo(EmploymentType.CONTRACT);
        assertThat(job.remote()).isFalse();
    }
}
