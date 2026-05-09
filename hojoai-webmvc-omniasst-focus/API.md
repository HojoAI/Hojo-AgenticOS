# hojoai-webmvc-omniasst-focus — API Documentation

## Base URL

- **context-path**: `/omniasst-focus`
- Via gateway: `{gateway}/omniasst-focus/...`

All paths below are relative to the **context-path**.

---

## Schedule v1 — `/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/schedule/task` | Schedule task query |
| GET | `/api/v1/schedule/free` | Free time etc. |
| POST | `/api/v1/schedule/conversation/{userId}` | Schedule conversation, **SSE** (`text/event-stream`) |
| POST | `/api/v1/schedule/meeting/{userId}` | Meeting related |
| POST | `/api/v1/schedule` | Create/submit schedule |
| POST | `/api/v1/schedule/card/{userId}` | Schedule card |
| GET | `/api/v1/schedules/{userId}` | List |
| GET | `/api/v1/schedule/{userId}` | Detail |
| PATCH | `/api/v1/schedule/{userId}/{scheduleId}` | Partial update |

## Schedule v2 — `/api/v2`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v2/schedule/flight` | Flight itinerary |
| GET | `/api/v2/schedule/train` | Train itinerary |
| GET | `/api/v2/schedule/map` | Map related |
| GET | `/api/v2/schedule/type-icon` | Type icon |
| GET | `/api/v2/schedule/movie-name` | Movie name parsing etc. |
| DELETE | `/api/v2/schedule/{scheduleId}` | Delete schedule |
| POST | `/api/v2/schedule/{userId}` | Create (v2) |
| PUT | `/api/v2/schedule/{scheduleId}` | Full update |
| POST | `/api/v2/schedule/sys-sync/{userId}` | System sync |

---

## Internal Dependencies (Feign, not external APIs)

Clients like `ManagementAPI` request management domain paths (e.g., `/omniasst-management/api/...`), for inter-service calls only, **not as external endpoints of this service**.