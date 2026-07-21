package com.careeros.aggregator.application;

import java.util.List;

/** Outbound port for a secondary source that discovers jobs across employers. */
public interface AggregatorJobSource {
    String sourceName();

    List<AggregatedJob> discoverJobs();
}
