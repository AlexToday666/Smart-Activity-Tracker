CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    user_id     TEXT        NOT NULL,
    event_type  TEXT        NOT NULL,
    event_date  TIMESTAMPTZ NOT NULL,
    metadata    TEXT,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_events_user_id    ON events (user_id);
CREATE INDEX idx_events_event_type ON events (event_type);
CREATE INDEX idx_events_event_date ON events (event_date);


