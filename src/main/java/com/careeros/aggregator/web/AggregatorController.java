package com.careeros.aggregator.web;

import com.careeros.aggregator.application.AggregatorIngestionService;
import com.careeros.aggregator.application.AggregatorRunResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/aggregators/builtin-chicago")
@Profile({"local", "docker"})
@ConditionalOnProperty(prefix = "careeros.aggregators.builtin-chicago", name = "enabled", havingValue = "true")
@Tag(name = "Local Aggregators", description = "Feature-flagged personal-use discovery sources")
public class AggregatorController {

    private final AggregatorIngestionService service;

    public AggregatorController(AggregatorIngestionService service) {
        this.service = service;
    }

    @GetMapping("/status")
    @Operation(summary = "Show local Built In Chicago aggregator status")
    public Map<String, Object> status() {
        return Map.of("source", "BUILT_IN_CHICAGO", "enabled", true, "scheduled", false,
                "usage", "personal-local-only");
    }

    @PostMapping("/runs")
    @Operation(summary = "Run a bounded Built In Chicago discovery import")
    public AggregatorRunResult run() {
        return service.run();
    }
}
