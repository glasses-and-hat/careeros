-- Some providers return every eligible office in a single location value.
-- Preserve the complete list instead of rejecting or silently truncating it.
ALTER TABLE job_postings
    ALTER COLUMN location TYPE TEXT;
