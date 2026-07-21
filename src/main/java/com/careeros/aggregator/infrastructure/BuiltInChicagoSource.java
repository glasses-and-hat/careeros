package com.careeros.aggregator.infrastructure;

import com.careeros.aggregator.application.AggregatedJob;
import com.careeros.aggregator.application.AggregatorJobSource;
import com.careeros.job.domain.EmploymentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;

@Component
@Profile({"local", "docker"})
@ConditionalOnProperty(prefix = "careeros.aggregators.builtin-chicago", name = "enabled", havingValue = "true")
public class BuiltInChicagoSource implements AggregatorJobSource {

    private static final String USER_AGENT = "CareerOS/0.1 (personal local job discovery)";

    private final RestClient client;
    private final ObjectMapper mapper;
    private final BuiltInChicagoProperties properties;

    public BuiltInChicagoSource(RestClient.Builder builder, ObjectMapper mapper,
                                BuiltInChicagoProperties properties) {
        this.client = builder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT).build();
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public String sourceName() {
        return "BUILT_IN_CHICAGO";
    }

    @Override
    public List<AggregatedJob> discoverJobs() {
        var listing = fetch(properties.baseUrl() + properties.searchPath());
        var urls = new LinkedHashSet<String>();
        for (var link : listing.select("a[data-id=job-card-title]")) {
            if (urls.size() >= properties.maxJobs()) break;
            urls.add(link.absUrl("href").isBlank() ? properties.baseUrl() + link.attr("href") : link.absUrl("href"));
        }
        var jobs = new ArrayList<AggregatedJob>();
        for (var url : urls) {
            pace();
            jobs.add(parseDetail(fetch(url), url));
        }
        return jobs;
    }

    private Document fetch(String url) {
        var html = client.get().uri(url).retrieve().body(String.class);
        if (html == null || html.isBlank()) throw new IllegalStateException("Empty response from " + url);
        return Jsoup.parse(html, url);
    }

    AggregatedJob parseDetail(Document document, String sourceUrl) {
        var posting = jobPostingSchema(document);
        var externalId = text(posting.path("identifier"), "value");
        var company = text(posting.path("hiringOrganization"), "name");
        var companyUrl = nullableText(posting.path("hiringOrganization"), "sameAs");
        var title = text(posting, "title");
        var description = text(posting, "description");
        var location = location(posting.path("jobLocation"));
        var employmentType = employmentType(text(posting, "employmentType"));
        var salary = posting.path("baseSalary").path("value");
        var currency = nullableText(posting.path("baseSalary"), "currency");
        var applyUrl = canonicalUrl(applyUrl(document, sourceUrl));
        var postedDate = date(nullableText(posting, "datePosted"));
        var remote = (description + " " + document.title()).toLowerCase(Locale.ROOT)
                .matches("(?s).*(#remote|fully remote|hiring remotely|remote position|remote or hybrid).*" );
        return new AggregatedJob(externalId, company, companyUrl == null || companyUrl.isBlank() ? sourceUrl : companyUrl, title,
                location, employmentType, remote, decimal(salary, "minValue"), decimal(salary, "maxValue"),
                currency, description, postedDate, applyUrl, sourceUrl);
    }

    private JsonNode jobPostingSchema(Document document) {
        for (var script : document.select("script")) {
            var data = script.data();
            if (!data.contains("\"JobPosting\"")) continue;
            try {
                var root = mapper.readTree(data);
                var graph = root.path("@graph");
                if (graph.isArray()) {
                    for (var node : graph) if ("JobPosting".equals(node.path("@type").asText())) return node;
                }
                if ("JobPosting".equals(root.path("@type").asText())) return root;
            } catch (Exception exception) {
                throw new IllegalStateException("Invalid Built In Chicago structured job data", exception);
            }
        }
        throw new IllegalStateException("Built In Chicago page did not contain JobPosting structured data");
    }

    private String applyUrl(Document document, String fallback) {
        for (var script : document.select("script")) {
            var data = script.data();
            var marker = "Builtin.jobPostInit(";
            var start = data.indexOf(marker);
            if (start < 0) continue;
            var end = data.lastIndexOf(");");
            if (end <= start) continue;
            try {
                var root = mapper.readTree(data.substring(start + marker.length(), end));
                var value = root.path("job").path("howToApply").asText();
                if (!value.isBlank()) return value;
            } catch (Exception exception) {
                throw new IllegalStateException("Invalid Built In Chicago application metadata", exception);
            }
        }
        return fallback;
    }

    private static String location(JsonNode node) {
        if (node.isArray()) {
            var values = new ArrayList<String>();
            node.forEach(value -> values.add(location(value)));
            return String.join("; ", values);
        }
        var address = node.path("address");
        var parts = new ArrayList<String>();
        add(parts, address.path("addressLocality").asText());
        add(parts, address.path("addressRegion").asText());
        add(parts, address.path("addressCountry").asText());
        return String.join(", ", parts);
    }

    private static EmploymentType employmentType(String value) {
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "FULL_TIME" -> EmploymentType.FULL_TIME;
            case "PART_TIME" -> EmploymentType.PART_TIME;
            case "CONTRACTOR", "CONTRACT" -> EmploymentType.CONTRACT;
            case "INTERN", "INTERNSHIP" -> EmploymentType.INTERNSHIP;
            default -> null;
        };
    }

    private static String canonicalUrl(String value) {
        try {
            var uri = URI.create(value);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (Exception ignored) {
            return value;
        }
    }

    private void pace() {
        if (!properties.requestDelay().isZero()) LockSupport.parkNanos(properties.requestDelay().toNanos());
    }

    private static LocalDate date(String value) {
        try { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
        catch (Exception ignored) { return null; }
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).decimalValue() : null;
    }

    private static String text(JsonNode node, String field) {
        var value = nullableText(node, field);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing Built In Chicago field: " + field);
        return value;
    }

    private static String nullableText(JsonNode node, String field) {
        var value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static void add(List<String> values, String value) {
        if (value != null && !value.isBlank()) values.add(value);
    }
}
