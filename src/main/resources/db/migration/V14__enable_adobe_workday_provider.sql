-- Adobe's branded careers search is backed by its public Workday tenant.
-- Use the CXS feed directly because the careers.adobe.com results page is
-- client-rendered and does not expose reliable job elements to Generic HTML.
UPDATE companies
SET ats_type = 'WORKDAY',
    ats_identifier = 'adobe/external_experienced',
    provider_type = 'WORKDAY',
    provider_configuration = '{"host":"adobe.wd5.myworkdayjobs.com"}',
    career_url = 'https://adobe.wd5.myworkdayjobs.com/en-US/external_experienced',
    enabled = TRUE,
    updated_at = now()
WHERE lower(name) = 'adobe';
