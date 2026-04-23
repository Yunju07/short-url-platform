# Backend API Spec (Current)

This document summarizes the public HTTP APIs exposed by the backend services in this repository.

## Base URLs (Local)

- `url-service`: `http://localhost:8081`
- `redirect-service`: `http://localhost:8082`
- `stats-service`: `http://localhost:8083`

## url-service

### POST /api/v2/urls

Create a short URL.

Request body:

```json
{
  "originalUrl": "https://example.com/auth/login",
  "ttl": 2592000
}
```

Notes:
- `ttl` is optional. Unit: seconds.

Response (200):

```json
{
  "isSuccess": true,
  "code": "...",
  "message": "...",
  "result": {
    "shortKey": "abc123",
    "shortUrl": "https://short.example.com/abc123",
    "originalUrl": "https://example.com/auth/login",
    "createdAt": "2025-01-01T00:00:00",
    "expiredAt": "2025-01-31T00:00:00"
  }
}
```

### GET /api/v2/urls/{key}

Get short URL detail (including click summary).

Response (200):

```json
{
  "isSuccess": true,
  "code": "...",
  "message": "...",
  "result": {
    "shortKey": "abc123",
    "shortUrl": "https://short.example.com/abc123",
    "originalUrl": "https://example.com/auth/login",
    "createdAt": "2025-01-01T00:00:00",
    "expiredAt": "2025-01-31T00:00:00",
    "clickSummary": {
      "totalClicks": 123,
      "lastClickedAt": "2025-01-02T00:00:00"
    }
  }
}
```

## redirect-service

### GET /{shortKey}

HTTP redirect.

Response:
- 302 with `Location: {originalUrl}`
- Error cases may return non-302 (implementation-dependent).

## stats-service

### GET /api/v2/stats/urls/{shortKey}

Get aggregated statistics for a short key.

### GET /api/v2/stats/top?date=YYYY-MM-DD&limit=10

Get daily top stats.

Notes:
- The stats are batch-aggregated; the response includes metadata about last aggregation time and interval.

