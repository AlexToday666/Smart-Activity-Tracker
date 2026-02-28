# Data Model

```mermaid
erDiagram
    PROJECTS ||--o{ API_KEYS : owns
    PROJECTS ||--o{ EVENTS : scopes
    PROJECTS {
      bigint id PK
      text name
      text slug UK
      text description
      timestamptz created_at
      timestamptz updated_at
    }
    API_KEYS {
      bigint id PK
      bigint project_id FK
      text key_hash UK
      text name
      boolean active
      timestamptz created_at
      timestamptz last_used_at
      timestamptz revoked_at
    }
    EVENTS {
      bigint id PK
      bigint project_id FK
      text event_id
      text user_id
      text event_type
      timestamptz occurred_at
      timestamptz received_at
      jsonb metadata
      text source
      text session_id
    }
```

## Indexes

- `unique(project_id, event_id)` enforces idempotency per project.
- `(project_id, occurred_at)` supports time-range analytics.
- `(project_id, event_type)` supports funnels and top event types.
- `(project_id, user_id)` supports user timelines and retention.
- `GIN(metadata)` supports metadata-aware search.
- `(project_id, source)` and `(project_id, session_id)` support operational filters.

## API Keys

Only SHA-256 hashes are stored in `api_keys.key_hash`. Raw secrets are generated with `SecureRandom`, returned once and then discarded.
