ALTER TABLE companies ADD COLUMN provider_type VARCHAR(32);
ALTER TABLE companies ADD COLUMN provider_configuration TEXT;

UPDATE companies SET provider_type = ats_type WHERE ats_type <> 'OTHER';

CREATE TABLE company_fallback_providers
(
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    provider_type VARCHAR(32) NOT NULL,
    CONSTRAINT chk_fallback_provider_type CHECK (provider_type IN
      ('GREENHOUSE','LEVER','ASHBY','WORKDAY','SMARTRECRUITERS','GENERIC_HTML','GENERIC_RSS','GENERIC_JSON','CUSTOM_PROVIDER'))
);
CREATE INDEX idx_company_fallback_providers_company ON company_fallback_providers(company_id);

CREATE TABLE sync_history
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    provider VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    duration_ms BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    jobs_found INTEGER NOT NULL DEFAULT 0,
    jobs_added INTEGER NOT NULL DEFAULT 0,
    jobs_updated INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sync_history_company_completed ON sync_history(company_id, completed_at DESC);
CREATE INDEX idx_sync_history_provider_completed ON sync_history(provider, completed_at DESC);
CREATE INDEX idx_sync_history_success ON sync_history(success);
