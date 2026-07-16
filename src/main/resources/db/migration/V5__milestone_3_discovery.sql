ALTER TABLE user_preferences ADD COLUMN salary_min NUMERIC(12,2);
ALTER TABLE user_preferences ADD COLUMN salary_max NUMERIC(12,2);
ALTER TABLE user_preferences ADD COLUMN salary_currency VARCHAR(3);
ALTER TABLE user_preferences ADD COLUMN visa_sponsorship_preferred BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE user_preference_ignored_companies(user_preference_id UUID NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE, company_name VARCHAR(255) NOT NULL);
CREATE TABLE user_preference_ignored_keywords(user_preference_id UUID NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE, keyword VARCHAR(255) NOT NULL);

CREATE TABLE watchlists(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),name VARCHAR(255) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT now(),updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),CONSTRAINT uq_watchlists_name UNIQUE(name));
CREATE TABLE watchlist_companies(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,priority INTEGER NOT NULL DEFAULT 50,enabled BOOLEAN NOT NULL DEFAULT TRUE,CONSTRAINT uq_watchlist_company UNIQUE(watchlist_id,company_id),CONSTRAINT chk_watchlist_priority CHECK(priority BETWEEN 0 AND 100));
CREATE INDEX idx_watchlist_companies_company ON watchlist_companies(company_id,enabled);

CREATE TABLE saved_searches(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),name VARCHAR(255) NOT NULL,title VARCHAR(500),sorting VARCHAR(255),minimum_score INTEGER,remote BOOLEAN,created_at TIMESTAMPTZ NOT NULL DEFAULT now(),updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),CONSTRAINT chk_saved_search_score CHECK(minimum_score BETWEEN 0 AND 100));
CREATE TABLE saved_search_companies(saved_search_id UUID NOT NULL REFERENCES saved_searches(id) ON DELETE CASCADE,companies UUID NOT NULL);
CREATE TABLE saved_search_locations(saved_search_id UUID NOT NULL REFERENCES saved_searches(id) ON DELETE CASCADE,locations VARCHAR(255) NOT NULL);
CREATE TABLE saved_search_technologies(saved_search_id UUID NOT NULL REFERENCES saved_searches(id) ON DELETE CASCADE,technologies VARCHAR(255) NOT NULL);

CREATE TABLE job_applications(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,status VARCHAR(32) NOT NULL,applied_date DATE,follow_up_date DATE,interview_date DATE,notes TEXT,resume_version VARCHAR(255),referral_name VARCHAR(255),recruiter_name VARCHAR(255),job_link VARCHAR(2048),created_at TIMESTAMPTZ NOT NULL DEFAULT now(),updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),CONSTRAINT uq_application_job UNIQUE(job_posting_id),CONSTRAINT chk_application_status CHECK(status IN('WISHLIST','APPLIED','RECRUITER_CONTACTED','REFERRAL_REQUESTED','RECRUITER_SCREEN','TECHNICAL_INTERVIEW','SYSTEM_DESIGN','BEHAVIORAL','FINAL_INTERVIEW','OFFER','REJECTED','WITHDRAWN')));
CREATE INDEX idx_applications_status ON job_applications(status);
CREATE INDEX idx_applications_follow_up ON job_applications(follow_up_date);
CREATE INDEX idx_applications_interview ON job_applications(interview_date);
