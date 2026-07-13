CREATE TABLE user_preferences
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    minimum_score  INTEGER     NOT NULL DEFAULT 0,
    remote_only    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_user_preferences_minimum_score CHECK (minimum_score BETWEEN 0 AND 100)
);

CREATE TABLE user_preference_roles
(
    user_preference_id UUID         NOT NULL REFERENCES user_preferences (id) ON DELETE CASCADE,
    role               VARCHAR(255) NOT NULL
);

CREATE INDEX idx_user_preference_roles_id ON user_preference_roles (user_preference_id);

CREATE TABLE user_preference_technologies
(
    user_preference_id UUID         NOT NULL REFERENCES user_preferences (id) ON DELETE CASCADE,
    technology         VARCHAR(255) NOT NULL
);

CREATE INDEX idx_user_preference_technologies_id ON user_preference_technologies (user_preference_id);

CREATE TABLE user_preference_locations
(
    user_preference_id UUID         NOT NULL REFERENCES user_preferences (id) ON DELETE CASCADE,
    location           VARCHAR(255) NOT NULL
);

CREATE INDEX idx_user_preference_locations_id ON user_preference_locations (user_preference_id);
