# ADR 0002: Project-Scoped API Keys

Status: accepted.

API keys are scoped to projects so event data is isolated at ingestion time. The server stores only key hashes and returns the raw secret once at creation.
