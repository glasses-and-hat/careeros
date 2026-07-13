CREATE TABLE companies
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    career_url VARCHAR(2048) NOT NULL,
    ats_type   VARCHAR(32)  NOT NULL,
    priority   VARCHAR(16)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_companies_name UNIQUE (name),
    CONSTRAINT chk_companies_ats_type CHECK (ats_type IN ('GREENHOUSE', 'LEVER', 'ASHBY', 'WORKDAY', 'SMARTRECRUITERS', 'OTHER')),
    CONSTRAINT chk_companies_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_companies_enabled ON companies (enabled);
CREATE INDEX idx_companies_ats_type ON companies (ats_type);
CREATE INDEX idx_companies_priority ON companies (priority);
