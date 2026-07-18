-- NVIDIA uses a numbered Workday shard. The Workday adapter reads the full
-- public host from provider_configuration while retaining tenant/site as its
-- stable provider identifier.
UPDATE companies
SET ats_type = 'WORKDAY',
    ats_identifier = 'nvidia/NVIDIAExternalCareerSite',
    provider_type = 'WORKDAY',
    provider_configuration = '{"host":"nvidia.wd5.myworkdayjobs.com"}',
    career_url = 'https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite',
    enabled = TRUE,
    updated_at = now()
WHERE lower(name) = 'nvidia';
