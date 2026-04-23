# AI Service API Spec (Draft)

This document defines the API contract between `url-service` and `ai-service`.

## Overview

- Purpose: Generate a `shortKey` for a given `originalUrl`.
- `ai-service` owns provider/model details (Ollama now, vLLM later).
- `url-service` must not depend on provider-specific APIs.
- Streaming: not used.

## Base URL

- Local (docker-compose): `http://ai-service:8000`
- Local (host): `http://localhost:8000`

## Endpoints

### POST /v1/shortkeys

Generate a short key for a URL.

Request body:

```json
{
  "originalUrl": "https://example.com/auth/login",
  "minLength": 6,
  "maxLength": 8,
  "alphabet": "0123456789abcdefghijklmnopqrstuvwxyz"
}
```

Response (200):

```json
{
  "shortKey": "login01",
  "provider": "ollama",
  "model": "phi3:mini",
  "traceId": "01J..."
}
```

Response fields:

- `shortKey` (string, required): candidate key.
- `provider` (string, optional): e.g. `ollama`, `vllm`.
- `model` (string, optional): model identifier.
- `traceId` (string, optional): request correlation id for logs/tracing.

Validation rules (server-side):

- `originalUrl`: required, must be a valid URL.
- `minLength`: required, integer.
- `maxLength`: required, integer, must be `>= minLength`.
- `alphabet`: required, must be a non-empty string of allowed characters.
- Output `shortKey` must:
  - have length within `[minLength, maxLength]`
  - contain only characters from `alphabet`

Error responses:

Response (400):

```json
{
  "code": "INVALID_REQUEST",
  "message": "minLength must be <= maxLength",
  "traceId": "01J..."
}
```

Response (502):

```json
{
  "code": "UPSTREAM_FAILURE",
  "message": "provider request failed",
  "traceId": "01J..."
}
```

Response (504):

```json
{
  "code": "TIMEOUT",
  "message": "provider timeout",
  "traceId": "01J..."
}
```

Response (500):

```json
{
  "code": "INTERNAL",
  "message": "unexpected error",
  "traceId": "01J..."
}
```

## Non-goals (for v1)

- Authentication/Authorization
- Rate limiting / quota
- Streaming responses
- Multi-provider routing policies exposed to clients
