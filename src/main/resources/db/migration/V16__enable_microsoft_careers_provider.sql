UPDATE companies
SET career_url = 'https://apply.careers.microsoft.com/careers',
    ats_type = 'OTHER',
    ats_identifier = NULL,
    provider_type = 'MICROSOFT_CAREERS',
    provider_configuration = '{"baseUrl":"https://apply.careers.microsoft.com","domain":"microsoft.com","query":"Senior Software Engineer","location":"United States","maxResults":100}',
    enabled = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(name) = 'microsoft';
