CREATE TABLE projects (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT        NOT NULL,
    slug        TEXT        NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_projects_slug UNIQUE (slug)
);

CREATE TABLE api_keys (
    id           BIGSERIAL PRIMARY KEY,
    project_id   BIGINT      NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    key_hash     TEXT        NOT NULL,
    name         TEXT        NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ,
    revoked_at   TIMESTAMPTZ,
    CONSTRAINT uk_api_keys_key_hash UNIQUE (key_hash)
);

INSERT INTO projects (name, slug, description)
VALUES ('Default Demo Project', 'default-demo', 'Project created by migration for legacy events');

ALTER TABLE events RENAME COLUMN event_date TO occurred_at;
ALTER TABLE events RENAME COLUMN created_at TO received_at;

ALTER TABLE events ADD COLUMN project_id BIGINT;
ALTER TABLE events ADD COLUMN event_id TEXT;
ALTER TABLE events ADD COLUMN source TEXT;
ALTER TABLE events ADD COLUMN session_id TEXT;

UPDATE events
SET project_id = (SELECT id FROM projects WHERE slug = 'default-demo'),
    event_id = 'legacy-' || id;

ALTER TABLE events
    ALTER COLUMN project_id SET NOT NULL,
    ALTER COLUMN event_id SET NOT NULL,
    ADD CONSTRAINT fk_events_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    ADD CONSTRAINT uk_events_project_event_id UNIQUE (project_id, event_id);

ALTER TABLE events
    ALTER COLUMN metadata TYPE jsonb
    USING CASE
        WHEN metadata IS NULL OR btrim(metadata) = '' THEN NULL
        WHEN metadata ~ '^\s*[\{\[]' THEN metadata::jsonb
        ELSE jsonb_build_object('value', metadata)
    END;

DROP INDEX IF EXISTS idx_events_user_id;
DROP INDEX IF EXISTS idx_events_event_type;
DROP INDEX IF EXISTS idx_events_event_date;

CREATE INDEX idx_events_project_occurred_at ON events (project_id, occurred_at);
CREATE INDEX idx_events_project_type ON events (project_id, event_type);
CREATE INDEX idx_events_project_user_id ON events (project_id, user_id);
CREATE INDEX idx_events_project_source ON events (project_id, source);
CREATE INDEX idx_events_project_session ON events (project_id, session_id);
CREATE INDEX idx_events_metadata_gin ON events USING GIN (metadata);
CREATE INDEX idx_api_keys_project_active ON api_keys (project_id, active);
