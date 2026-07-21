package com.careeros.resume.web;

import com.careeros.common.web.PageResponse;
import com.careeros.resume.application.ResumeTailoringService;
import com.careeros.resume.domain.ResumeVersion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private final ResumeTailoringService service;

    public ResumeController(ResumeTailoringService service) { this.service = service; }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Response generate(@Valid @RequestBody GenerateRequest request) {
        return out(service.generate(new ResumeTailoringService.Command(request.jobId, request.applicationId,
                request.model, request.notes, request.skills, request.softSkills, request.matchExplanation)));
    }

    @GetMapping
    public PageResponse<Response> list(Pageable pageable) { return PageResponse.from(service.list(pageable), this::out); }
    @GetMapping("/{id}")
    public Response get(@PathVariable UUID id) { return out(service.get(id)); }
    @GetMapping("/job/{jobId}")
    public List<Response> job(@PathVariable UUID jobId) { return service.history(jobId).stream().map(this::out).toList(); }

    @GetMapping("/{id}/download/docx")
    public ResponseEntity<Resource> downloadDocx(@PathVariable UUID id) {
        var path = service.artifact(id);
        return ResponseEntity.ok().contentType(DOCX)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                .body(new FileSystemResource(path));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID id) { service.archive(id); }
    @PostMapping("/{id}/restore")
    public Response restore(@PathVariable UUID id) { return out(service.restore(id)); }
    @GetMapping("/health")
    public ResumeTailoringService.Health health() { return service.health(); }

    private Response out(ResumeVersion version) {
        return new Response(version.getId(), version.getVersionNumber(), version.getStatus().name(),
                version.getJob() == null ? null : version.getJob().getId(),
                version.getJob() == null ? null : version.getJob().getTitle(), version.getApplicationId(),
                version.getModelUsed(), version.getPromptVersion(), version.getGenerationDurationMs(),
                version.getValidationResult(), lines(version.getValidationWarnings()), json(version.getGeneratedBullets()),
                version.isPdfGenerated(), version.isArchived(), version.getCreatedAt());
    }

    private List<String> lines(String value) { return value == null || value.isBlank() ? List.of() : Arrays.asList(value.split("\\n")); }
    @SuppressWarnings("unchecked")
    private List<String> json(String value) {
        try { return value == null ? List.of() : new com.fasterxml.jackson.databind.ObjectMapper().readValue(value, List.class); }
        catch (Exception ignored) { return List.of(); }
    }

    public record GenerateRequest(@NotNull UUID jobId, UUID applicationId, String model, String notes,
                                  List<String> skills, List<String> softSkills, String matchExplanation) {}
    public record Response(UUID id, int versionNumber, String status, UUID jobId, String jobTitle,
                           UUID applicationId, String modelUsed, String promptVersion, Long generationDurationMs,
                           String validationResult, List<String> validationWarnings, List<String> generatedBullets,
                           boolean pdfGenerated, boolean archived, Instant createdAt) {}
}
