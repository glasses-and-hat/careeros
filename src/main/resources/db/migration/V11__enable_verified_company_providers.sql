-- Enable catalog companies only after verifying that their public provider feed
-- returns a valid job board. This upgrades existing databases; the CSV seed
-- contains the same mappings for new installations.
WITH verified_company(name, provider_type, identifier) AS (
    VALUES
        ('Snowflake', 'ASHBY', 'snowflake'),
        ('Anthropic', 'GREENHOUSE', 'anthropic'),
        ('Plaid', 'ASHBY', 'plaid'),
        ('Postman', 'GREENHOUSE', 'postman'),
        ('CircleCI', 'GREENHOUSE', 'circleci'),
        ('Harness', 'GREENHOUSE', 'harnessinc'),
        ('Temporal', 'GREENHOUSE', 'temporaltechnologies'),
        ('New Relic', 'GREENHOUSE', 'newrelic'),
        ('Block (Square)', 'GREENHOUSE', 'block'),
        ('Brex', 'GREENHOUSE', 'brex'),
        ('Robinhood', 'GREENHOUSE', 'robinhood'),
        ('Coinbase', 'GREENHOUSE', 'coinbase'),
        ('Affirm', 'GREENHOUSE', 'affirm'),
        ('Perplexity', 'ASHBY', 'perplexity'),
        ('Scale AI', 'GREENHOUSE', 'scaleai'),
        ('Glean', 'GREENHOUSE', 'gleanwork'),
        ('Together AI', 'GREENHOUSE', 'togetherai'),
        ('Modal', 'ASHBY', 'modal'),
        ('ElevenLabs', 'ASHBY', 'elevenlabs'),
        ('Relativity', 'GREENHOUSE', 'relativity'),
        ('project44', 'GREENHOUSE', 'project44'),
        ('DRW', 'GREENHOUSE', 'drweng'),
        ('IMC Trading', 'GREENHOUSE', 'imc'),
        ('ClickHouse', 'GREENHOUSE', 'clickhouse'),
        ('Supabase', 'ASHBY', 'supabase'),
        ('Linear', 'ASHBY', 'linear'),
        ('Railway', 'ASHBY', 'railway'),
        ('Tailscale', 'GREENHOUSE', 'tailscale'),
        ('Orb', 'ASHBY', 'orb')
)
UPDATE companies c
SET ats_type = v.provider_type,
    ats_identifier = v.identifier,
    provider_type = v.provider_type,
    provider_configuration = NULL,
    career_url = CASE v.provider_type
        WHEN 'GREENHOUSE' THEN 'https://boards.greenhouse.io/' || v.identifier
        WHEN 'ASHBY' THEN 'https://jobs.ashbyhq.com/' || v.identifier
    END,
    enabled = TRUE,
    updated_at = now()
FROM verified_company v
WHERE lower(c.name) = lower(v.name);
