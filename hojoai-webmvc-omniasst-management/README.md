# hojoai-webmvc-omniasst-management

**Omniasst Management Service**: Aggregates management domain capabilities, including session and message, virtual avatar, push, object storage presigned URLs, message center, translation session list, recommendations, meetings, and voice print/speech configuration REST APIs.

## Capabilities

### Session and Message
- Session management: Create session, query list, delete session, update title
- Message management: Send/append messages, batch operations, message replacement and deletion
- Read status: Mark all sessions as read

### Virtual Avatar
- Avatar list: Query all available virtual avatars
- Current avatar: Get/switch user's current virtual avatar

### Push
- Message push: Push notifications (supports broadcast, user, device token routing)
- Device binding: User device token binding
- FCM debugging: Firebase Cloud Messaging push debugging

### Object Storage
- Presigned URL: Generate presigned URL for direct upload to OSS
- URL renewal: Extend presigned URL validity
- Append upload: Audio chunk append upload (resume from breakpoint scenario)

### Message Center
- Message list: Paginated query, read/unread count
- Message management: Single/batch mark as read, clear
- Task center: In-progress task tracking and completion

### Translation Session
- Session list: User translation session paginated query
- Message list: Translation session message query

### Recommendations
- Recommendation list: Query recommendations by type
- Recommendation management: Ignore recommendations, create recommendation entries

### Meeting and Voice Print
- Meeting query: Meeting list, detail query
- Voice print management: Register, match, query, delete voice print
- Voice cloning: Upload WAV audio to clone voice
- Voice print registration flow: Two-phase voice print registration (prepare + commit)

## Tech Stack

- Java 21, Spring Boot Web
- Inherits from [hojoai-parent](../hojoai-parent); includes Feign calls to downstream services such as voice gateway (see source code for internal details)

## Runtime Conventions

| Item | Description |
|------|-------------|
| Runnable module | `hojoai-webmvc-omniasst-management-server` |
| Default HTTP port | `12005` (subject to `application.properties`, do not conflict with gateway port during deployment) |
| Servlet context-path | `/omniasst-management/api` |
| Spring application name | `hojoai-webmvc-omniasst-management` |
| Via gateway | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-management/**`; full URL must include context-path, e.g., `{gateway}/omniasst-management/api/v0/...` |

## Build and Run

```bash
cd hojoai-webmvc-omniasst-management
mvn clean install
cd hojoai-webmvc-omniasst-management-server && mvn spring-boot:run
```

## Documentation

- [API.md](./API.md) — `/v0`, `/v1` resource path descriptions (paths in the table are Servlet paths with context-path removed).