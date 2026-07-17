CREATE TABLE resume_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_number INTEGER NOT NULL,
    type VARCHAR(24) NOT NULL,
    status VARCHAR(24) NOT NULL,
    job_posting_id UUID REFERENCES job_postings(id) ON DELETE SET NULL,
    application_id UUID REFERENCES job_applications(id) ON DELETE SET NULL,
    model_used VARCHAR(128),
    prompt_version VARCHAR(32) NOT NULL,
    generation_duration_ms BIGINT,
    validation_result VARCHAR(32),
    validation_warnings TEXT,
    generated_bullets TEXT,
    docx_path VARCHAR(2048),
    pdf_path VARCHAR(2048),
    pdf_generated BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    downloaded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_resume_version UNIQUE(version_number),
    CONSTRAINT chk_resume_type CHECK(type IN ('MASTER','GENERATED')),
    CONSTRAINT chk_resume_status CHECK(status IN ('GENERATING','READY','FAILED','ARCHIVED'))
);
CREATE INDEX idx_resume_job_created ON resume_versions(job_posting_id, created_at DESC);
CREATE INDEX idx_resume_application ON resume_versions(application_id);
CREATE INDEX idx_resume_status_created ON resume_versions(status, created_at DESC);

ALTER TABLE job_applications ADD COLUMN resume_version_id UUID REFERENCES resume_versions(id) ON DELETE SET NULL;
