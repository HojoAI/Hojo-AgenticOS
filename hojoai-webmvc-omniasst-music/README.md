# hojoai-webmvc-omniasst-music

**Omniasst Music Service**: Aggregates NetEase Cloud capabilities (song search, playback URL, lyrics, charts, etc.), provides OAuth2 authorization flow, and SSE-based music agent conversation capability.

## Capabilities

- NetEase Cloud Search: Song search, fuzzy search, tag search, charts
- Playback Capability: Playback URL retrieval, lyrics retrieval
- OAuth2 Authorization: Authorization URL generation, callback handling, token refresh, status query
- Music Agent: AI-driven natural language music queries, returned via SSE streaming
- Playback Data Reporting: Playback history reporting to NetEase Cloud

## Tech Stack

- Java 21, Spring Boot Web
- Inherits [hojoai-parent](../hojoai-parent); calls NetEase Cloud Open Platform via Feign, etc. (URL specified by configuration)

## Runtime Conventions

| Item | Description |
|------|------|
| Runnable Module | `hojoai-webmvc-omniasst-music-server` |
| Default HTTP Port | `12009` |
| Servlet context-path | `/omniasst-music` |
| Spring Application Name | `hojoai-webmvc-omniasst-music` |
| Via Gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-music/**` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-music
mvn clean install
cd hojoai-webmvc-omniasst-music-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — External paths `/netease-cloud`, `/oauth2`, `/music-agent`, etc.