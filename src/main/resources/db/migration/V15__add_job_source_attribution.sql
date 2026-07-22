ALTER TABLE job_postings
    ADD COLUMN source_type VARCHAR(32) NOT NULL DEFAULT 'DIRECT_PROVIDER',
    ADD COLUMN source_url TEXT;

CREATE INDEX idx_job_postings_apply_url ON job_postings (apply_url);
CREATE INDEX idx_job_postings_source_type ON job_postings (source_type);
