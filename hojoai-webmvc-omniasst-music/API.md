# hojoai-webmvc-omniasst-music — API Documentation

## Base URL

- **context-path**: `/omniasst-music`
- Via gateway: `{gateway}/omniasst-music/...`

---

## NetEase Cloud Aggregation — `/netease-cloud`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/netease-cloud/songs` | Song search |
| GET | `/netease-cloud/songs/bytag` | By tag |
| POST | `/netease-cloud/songs/fuzzy` | Fuzzy search |
| GET | `/netease-cloud/songs/toplist` | Charts |
| GET | `/netease-cloud/song/playurl` | Play URL |
| GET | `/netease-cloud/song/lyric` | Lyrics |
| POST | `/netease-cloud/play/data/record` | Play data reporting |

## OAuth2 — `/oauth2`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/oauth2/generate-auth-url` | Generate authorization URL |
| GET | `/oauth2/callback` | Authorization callback |
| POST | `/oauth2/refresh-token` | Refresh token |
| GET | `/oauth2/auth-status` | Authorization status |
| GET | `/oauth2/revoke` | Revoke authorization |

## Music Agent — `/music-agent`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/music-agent/query` | Agent query, **SSE** (`text/event-stream`) |

---

## Internal Feign (not external endpoints)

`NeteaseCloudMusicAPI` maps to NetEase Open Platform paths (e.g., `/openapi/music/basic/...`), with `url` configured externally. **Clients should use the external endpoints `/netease-cloud` and `/oauth2` above.**