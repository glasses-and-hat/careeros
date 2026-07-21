package com.careeros.aggregator.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "careeros.aggregators.builtin-chicago")
public record BuiltInChicagoProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("https://www.builtinchicago.org") String baseUrl,
        @DefaultValue("/jobs/dev-engineering/senior") String searchPath,
        @DefaultValue("25") int maxJobs,
        @DefaultValue("250ms") Duration requestDelay
) {
    public BuiltInChicagoProperties {
        if (maxJobs < 1 || maxJobs > 50) throw new IllegalArgumentException("maxJobs must be between 1 and 50");
        if (requestDelay.isNegative()) throw new IllegalArgumentException("requestDelay must not be negative");
    }
}
