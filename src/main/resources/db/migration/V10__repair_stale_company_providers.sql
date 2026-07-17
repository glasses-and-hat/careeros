-- Confluent moved its public board from Greenhouse to Ashby.
UPDATE companies
SET ats_type = 'ASHBY',
    ats_identifier = 'confluent',
    provider_type = 'ASHBY',
    career_url = 'https://jobs.ashbyhq.com/confluent',
    enabled = TRUE,
    updated_at = now()
WHERE lower(name) = 'confluent';

-- These catalog entries no longer expose jobs through their configured public
-- ATS identifiers. Keep them tracked, but do not report a known-broken source
-- as actively monitored. They can be re-enabled after provider configuration.
UPDATE companies
SET enabled = FALSE,
    updated_at = now()
WHERE lower(name) IN (
    'anthropic', 'atlassian', 'canva', 'hashicorp', 'plaid',
    'redis', 'rippling', 'snowflake', 'uber'
);
