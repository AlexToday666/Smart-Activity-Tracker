# ADR 0003: Idempotency by Project and Event ID

Status: accepted.

Idempotency is enforced by `unique(project_id, event_id)`. This allows clients to retry ingestion safely without creating duplicates and without coordinating globally unique event IDs across projects.
