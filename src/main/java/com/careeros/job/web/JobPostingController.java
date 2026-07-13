package com.careeros.job.web;

import com.careeros.common.web.PageResponse;
import com.careeros.job.application.JobPostingService;
import com.careeros.job.domain.EmploymentType;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.JobPostingFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CRUD API for normalized job postings.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs", description = "Manage normalized job postings discovered from monitored companies")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @PostMapping
    @Operation(summary = "Create a job posting")
    public ResponseEntity<JobPostingResponse> create(@Valid @RequestBody JobPostingRequest request) {
        JobPosting jobPosting = jobPostingService.create(JobPostingMapper.toCommand(request));
        JobPostingResponse response = JobPostingMapper.toResponse(jobPosting);
        return ResponseEntity.created(URI.create("/api/v1/jobs/" + jobPosting.getId())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a job posting by id")
    public JobPostingResponse get(@PathVariable UUID id) {
        return JobPostingMapper.toResponse(jobPostingService.get(id));
    }

    @GetMapping
    @Operation(summary = "List job postings with optional filtering, sorting, and pagination")
    public PageResponse<JobPostingResponse> list(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) LocalDate postedAfter,
            @PageableDefault(size = 20, sort = "postedDate") Pageable pageable) {
        JobPostingFilter filter = new JobPostingFilter(companyId, title, location, employmentType, remote, postedAfter);
        return PageResponse.from(jobPostingService.list(filter, pageable), JobPostingMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing job posting")
    public JobPostingResponse update(@PathVariable UUID id, @Valid @RequestBody JobPostingRequest request) {
        JobPosting jobPosting = jobPostingService.update(id, JobPostingMapper.toCommand(request));
        return JobPostingMapper.toResponse(jobPosting);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job posting")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        jobPostingService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
