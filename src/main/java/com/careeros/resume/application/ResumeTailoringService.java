package com.careeros.resume.application;

import com.careeros.ai.application.AIProvider;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.documents.application.ResumeDocumentPort;
import com.careeros.documents.application.ResumeArtifactPathFactory;
import com.careeros.documents.infrastructure.ResumeDocumentProperties;
import com.careeros.job.domain.JobPostingRepository;
import com.careeros.prompt.application.PromptTemplateService;
import com.careeros.resume.domain.ResumeVersion;
import com.careeros.resume.domain.ResumeVersionRepository;
import com.careeros.validation.application.ResumeGroundingValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ResumeTailoringService {
    private final ResumeVersionRepository versions;
    private final JobPostingRepository jobs;
    private final AIProvider ai;
    private final ResumeDocumentPort documents;
    private final ResumeDocumentProperties config;
    private final ResumeArtifactPathFactory artifactPaths;
    private final PromptTemplateService prompts;
    private final ResumeGroundingValidator validator;
    private final ObjectMapper json;

    public ResumeTailoringService(ResumeVersionRepository versions, JobPostingRepository jobs, AIProvider ai,
                                  ResumeDocumentPort documents, ResumeDocumentProperties config,
                                  ResumeArtifactPathFactory artifactPaths, PromptTemplateService prompts,
                                  ResumeGroundingValidator validator, ObjectMapper json) {
        this.versions = versions;
        this.jobs = jobs;
        this.ai = ai;
        this.documents = documents;
        this.config = config;
        this.artifactPaths = artifactPaths;
        this.prompts = prompts;
        this.validator = validator;
        this.json = json;
    }

    public ResumeVersion generate(Command command) {
        var job = jobs.findById(command.jobId())
                .orElseThrow(() -> ResourceNotFoundException.forId("JobPosting", command.jobId()));
        var model = command.model() == null || command.model().isBlank() ? "llama3.1:8b" : command.model();
        var version = versions.save(ResumeVersion.generating(versions.nextVersion(), job,
                command.applicationId(), model, PromptTemplateService.VERSION));
        var start = Instant.now();

        try {
            var master = Path.of(require(config.masterPath(), "MASTER_RESUME_PATH is not configured"));
            var extracted = documents.extract(master);
            var prompt = prompts.render(Map.of(
                    "master_resume", extracted.text(),
                    "job_title", job.getTitle(),
                    "job_description", Objects.toString(job.getDescription(), ""),
                    "match_explanation", Objects.toString(command.matchExplanation(), ""),
                    "notes", Objects.toString(command.notes(), ""),
                    "skills", String.join(", ", safe(command.skills())),
                    "soft_skills", String.join(", ", safe(command.softSkills()))));

            var generated = ai.generateJson(model, prompt);
            var validation = validator.validate(extracted.text(), generated.bullets());
            if (!validation.valid()) {
                generated = ai.generateJson(model, prompt + "\nVALIDATION FAILURE: "
                        + String.join("; ", validation.warnings())
                        + ". Retry once and return a non-empty rewrite for every bullet using only exact facts from MASTER RESUME.");
                validation = validator.validate(extracted.text(), generated.bullets());
            }

            List<String> bullets = validation.valid() ? generated.bullets() : extracted.bullets();
            var warnings = new ArrayList<>(generated.warnings());
            warnings.addAll(validation.warnings());
            if (!validation.valid()) warnings.add("Conservative fallback used original master-resume bullets");

            var paths = artifactPaths.create(Path.of(config.outputDirectory()).toAbsolutePath(),
                    job.getCompany().getName(), LocalDate.now(), job.getId(), version.getVersionNumber(),
                    config.artifactBaseName());
            var docx = documents.generate(master, paths.docx(), bullets);
            version.complete(Duration.between(start, Instant.now()).toMillis(),
                    validation.valid() ? "VALID" : "CONSERVATIVE_FALLBACK", join(warnings), write(bullets),
                    docx.toString(), null);
            var saved = versions.save(version);
            return versions.findById(saved.getId()).orElseThrow();
        } catch (Exception exception) {
            version.fail(Duration.between(start, Instant.now()).toMillis(), exception.getMessage());
            versions.save(version);
            throw exception;
        }
    }

    public Page<ResumeVersion> list(Pageable pageable) { return versions.findAll(pageable); }
    public List<ResumeVersion> history(UUID jobId) { return versions.findByJobId(jobId); }
    public ResumeVersion get(UUID id) { return versions.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("ResumeVersion", id)); }
    public void archive(UUID id) { var version = get(id); version.archive(); versions.save(version); }
    public ResumeVersion restore(UUID id) { var version = get(id); version.restore(); versions.save(version); return get(id); }

    public Path artifact(UUID id) {
        var version = get(id);
        if (version.getDocxPath() == null) throw new IllegalStateException("DOCX artifact is unavailable");
        var path = Path.of(version.getDocxPath());
        if (!Files.isRegularFile(path)) throw new IllegalStateException("Resume artifact is missing: " + path);
        version.downloaded();
        versions.save(version);
        return path;
    }

    public Health health() { return new Health(ai.name(), ai.healthy(), ai.models(), "Resume processing stays on this machine"); }
    private String write(Object value) { try { return json.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private String join(List<String> values) { return String.join("\n", values); }
    private String require(String value, String message) { if (value == null || value.isBlank()) throw new IllegalStateException(message); return value; }
    private <T> List<T> safe(List<T> values) { return values == null ? List.of() : values; }

    public record Command(UUID jobId, UUID applicationId, String model, String notes, List<String> skills,
                          List<String> softSkills, String matchExplanation) {}
    public record Health(String provider, boolean healthy, List<String> models, String privacy) {}
}
