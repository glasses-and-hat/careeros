package com.careeros.aggregator.application;

import java.util.List;

public record AggregatorRunResult(
        String source,
        int discovered,
        int created,
        int updated,
        int skipped,
        int failed,
        int companiesCreated,
        List<String> errors
) {
}
