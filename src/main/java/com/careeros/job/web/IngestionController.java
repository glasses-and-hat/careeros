package com.careeros.job.web;

import com.careeros.job.application.ingestion.JobIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Manually triggers ATS job posting ingestion, independent of the optional
 * scheduled poller (see {@code careeros.ingestion.enabled}).
 */
@RestController
@RequestMapping("/api/v1/ingestion")
@Tag(name = "Ingestion", description = "Manually trigger ATS job posting ingestion")
public class IngestionController {

    private final JobIngestionService jobIngestionService;

    public IngestionController(JobIngestionService jobIngestionService) {
        this.jobIngestionService = jobIngestionService;
    }

    @PostMapping("/runs")
    @Operation(summary = "Run ingestion for all enabled companies")
    public IngestionRunResponse runAll() {
        return IngestionResponseMapper.toResponse(jobIngestionService.ingestAllEnabledCompanies());
    }

    @PostMapping("/companies/{companyId}/runs")
    @Operation(summary = "Run ingestion for a single company by id, regardless of its enabled flag")
    public IngestionRunResponse runForCompany(@PathVariable UUID companyId) {
        return IngestionResponseMapper.toResponse(jobIngestionService.ingestCompany(companyId));
    }
}
