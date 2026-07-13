CREATE TABLE job_postings
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id     VARCHAR(255)  NOT NULL,
    company_id      UUID          NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    title           VARCHAR(500)  NOT NULL,
    location        VARCHAR(255),
    employment_type VARCHAR(32),
    remote          BOOLEAN       NOT NULL DEFAULT FALSE,
    salary_min      NUMERIC(12, 2),
    salary_max      NUMERIC(12, 2),
    salary_currency VARCHAR(3),
    description     TEXT,
    posted_date     DATE,
    apply_url       VARCHAR(2048) NOT NULL,
    hash            VARCHAR(64)   NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_job_postings_hash UNIQUE (hash),
    CONSTRAINT uq_job_postings_company_external_id UNIQUE (company_id, external_id),
    CONSTRAINT chk_job_postings_employment_type CHECK (
        employment_type IS NULL OR employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY')
    )
);

CREATE INDEX idx_job_postings_company_id ON job_postings (company_id);
CREATE INDEX idx_job_postings_remote ON job_postings (remote);
CREATE INDEX idx_job_postings_posted_date ON job_postings (posted_date);
CREATE INDEX idx_job_postings_title ON job_postings (title);
