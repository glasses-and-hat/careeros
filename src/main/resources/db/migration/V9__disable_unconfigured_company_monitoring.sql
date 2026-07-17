-- Legacy OTHER companies were seeded as enabled even though they had no
-- provider. They remain tracked/watchlisted and can be enabled after a
-- provider configuration is supplied through the Companies UI.
UPDATE companies
SET enabled = FALSE,
    updated_at = now()
WHERE provider_type IS NULL
  AND enabled = TRUE;
