# ADR 0001: JSONB Metadata

Status: accepted.

Metadata is stored as PostgreSQL `jsonb` because event properties differ by product area and SDK. JSONB keeps ingestion flexible while still allowing GIN indexes and key-based filters such as `metadata.country=DE`.
