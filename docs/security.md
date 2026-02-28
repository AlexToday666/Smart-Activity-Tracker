# Security

## API Key Authentication

Ingestion endpoints require `X-API-Key`. The application hashes the submitted key with SHA-256 and looks up an active key. The raw key is never persisted and never logged.

Lifecycle:

1. Create a project.
2. Create an API key for the project.
3. Store the returned `secret` in the calling system.
4. Use `X-API-Key` for single or batch ingestion.
5. Revoke the key when it should no longer ingest data.

## Demo Management Endpoints

Project and API-key management endpoints are intentionally unauthenticated in this pet-project version to keep local demos simple. A production deployment should place these endpoints behind JWT/RBAC or an API gateway.

## Safe Logging

Logs may include `projectId` and `apiKeyId`. They must not include raw API keys, passwords or bearer tokens.
