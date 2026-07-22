package com.careeros.aggregator.infrastructure;

import com.careeros.aggregator.application.AggregatedJob;
import com.careeros.aggregator.application.AggregatorJobSource;
import com.careeros.job.domain.EmploymentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@Profile({"local", "docker"})
@ConditionalOnProperty(prefix = "careeros.aggregators.builtin-chicago", name = "enabled", havingValue = "true")
public class BuiltInChicagoSource implements AggregatorJobSource {
    private static final String USER_AGENT = "CareerOS/0.1 (personal local job discovery)";
    private static final Pattern RELATIVE_AGE = Pattern.compile("(?i)(?:re)?posted\\s+(\\d+)\\s+(hour|day|week|month)s?\\s+ago");
    private static final Pattern SALARY = Pattern.compile("(?i)([0-9]+(?:\\.[0-9]+)?[km]?)\\s*-\\s*([0-9]+(?:\\.[0-9]+)?[km]?)");

    private final RestClient client;
    private final BuiltInChicagoProperties properties;

    public BuiltInChicagoSource(RestClient.Builder builder, BuiltInChicagoProperties properties) {
        this.client = builder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT).build();
        this.properties = properties;
    }

    @Override
    public String sourceName() { return "BUILT_IN_CHICAGO"; }

    @Override
    public List<AggregatedJob> discoverJobs() {
        return parseListing(fetch(properties.baseUrl() + properties.searchPath()));
    }

    private Document fetch(String url) {
        var html = client.get().uri(url).retrieve().body(String.class);
        if (html == null || html.isBlank()) throw new IllegalStateException("Empty response from " + url);
        return Jsoup.parse(html, url);
    }

    List<AggregatedJob> parseListing(Document listing) {
        var jobs = new ArrayList<AggregatedJob>();
        for (var card : listing.select("[data-id=job-card]")) {
            if (jobs.size() >= properties.maxJobs()) break;
            var parsed = parseCard(card);
            if (parsed != null) jobs.add(parsed);
        }
        if (jobs.isEmpty()) {
            throw new IllegalStateException("Built In Chicago listing contained no recognizable job cards; its markup may have changed");
        }
        return List.copyOf(jobs);
    }

    private AggregatedJob parseCard(Element card) {
        var titleLink = card.selectFirst("a[data-id=job-card-title]");
        var companyLink = card.selectFirst("a[data-id=company-title]");
        if (titleLink == null || companyLink == null || titleLink.text().isBlank() || companyLink.text().isBlank()) {
            return null;
        }
        var sourceUrl = absoluteUrl(titleLink);
        var externalId = titleLink.attr("data-builtin-track-job-id");
        if (externalId.isBlank()) externalId = sourceUrl.substring(sourceUrl.lastIndexOf('/') + 1);
        var arrangement = textNearIcon(card, "fa-house-building");
        var description = text(card.selectFirst("[id^=drop-data-] .fs-sm.fw-regular.mb-md.text-gray-04"));
        var salary = salary(textNearIcon(card, "fa-sack-dollar"));
        var title = titleLink.text().trim();
        var remoteText = (arrangement + " " + title + " " + description).toLowerCase(Locale.ROOT);
        var remote = remoteText.contains("remote") && !remoteText.contains("hybrid");
        return new AggregatedJob(externalId, companyLink.text().trim(), absoluteUrl(companyLink), title,
                textNearIcon(card, "fa-location-dot"), employmentType(title), remote,
                salary.min(), salary.max(), salary.currency(), description,
                postedDate(textNearIcon(card, "fa-clock")), sourceUrl, sourceUrl);
    }

    private static String absoluteUrl(Element link) {
        var absolute = link.absUrl("href");
        return absolute.isBlank() ? link.attr("href") : absolute;
    }

    private static String textNearIcon(Element card, String iconClass) {
        var icon = card.selectFirst("i." + iconClass);
        if (icon == null) return "";
        var parent = icon.parent();
        var container = parent == null ? null : parent.parent();
        if (container == null) return text(parent);
        var value = container.selectFirst("span.font-barlow.text-gray-04");
        return value == null ? container.text().trim() : value.text().trim();
    }

    private static String text(Element element) { return element == null ? "" : element.text().trim(); }

    private static EmploymentType employmentType(String title) {
        var normalized = title.toLowerCase(Locale.ROOT);
        if (normalized.contains("intern")) return EmploymentType.INTERNSHIP;
        if (normalized.contains("contract")) return EmploymentType.CONTRACT;
        if (normalized.contains("part time") || normalized.contains("part-time")) return EmploymentType.PART_TIME;
        return EmploymentType.FULL_TIME;
    }

    private static Salary salary(String value) {
        var matcher = SALARY.matcher(value == null ? "" : value);
        if (!matcher.find()) return new Salary(null, null, null);
        return new Salary(amount(matcher.group(1)), amount(matcher.group(2)), "USD");
    }

    private static BigDecimal amount(String value) {
        var normalized = value.toLowerCase(Locale.ROOT);
        var multiplier = normalized.endsWith("k") ? BigDecimal.valueOf(1_000)
                : normalized.endsWith("m") ? BigDecimal.valueOf(1_000_000) : BigDecimal.ONE;
        if (!multiplier.equals(BigDecimal.ONE)) normalized = normalized.substring(0, normalized.length() - 1);
        return new BigDecimal(normalized).multiply(multiplier);
    }

    private static LocalDate postedDate(String value) {
        var matcher = RELATIVE_AGE.matcher(value == null ? "" : value);
        if (!matcher.find()) return LocalDate.now();
        var count = Long.parseLong(matcher.group(1));
        return switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
            case "day" -> LocalDate.now().minusDays(count);
            case "week" -> LocalDate.now().minusWeeks(count);
            case "month" -> LocalDate.now().minusMonths(count);
            default -> LocalDate.now();
        };
    }

    private record Salary(BigDecimal min, BigDecimal max, String currency) {}
}
