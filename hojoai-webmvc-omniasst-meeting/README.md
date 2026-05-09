# hojoai-webmvc-omniasst-meeting

**Omniasst Meeting Service**: Provides real-time meeting transcription, speaker recognition, and audio processing capabilities.

## Capabilities

- Real-time speech recognition: Real-time transcription of meeting audio, supports WebSocket streaming output
- Speaker recognition: Identify different speakers based on voice print technology, supports speaker separation and tracking
- Audio segment processing: Intelligent segmentation, organize meeting text by speaker
- OSS storage: Meeting audio automatically uploaded to OSS for storage
- Voice cloning: Upload WAV audio to clone user voice
- Voice print registration: Two-phase voice print registration process (upload + commit)
- Meeting import: Async transcription of existing audio files on OSS, supports batch import
- Meeting management: Meeting list query, detail aggregation, participant management
- Meeting control: Pause/resume/stop meeting recording

## Tech Stack

- Java 21, Spring Boot 3.x (Web, **WebSocket**, Actuator)
- **Spring Data Redis**, **OpenFeign** + LoadBalancer
- Inherits from [hojoai-parent](../hojoai-parent)

## Architecture

Meeting Service (this service) → Voice Foundation Service → Algorithm Provider

See Voice Foundation Service HTTP API documentation for details.

## Quick Start

```bash
cd hojoai-webmvc-omniasst-meeting
mvn clean package -DskipTests
java -jar hojoai-webmvc-omniasst-meeting-server/target/hojoai-webmvc-omniasst-meeting-server-1.0.0-SNAPSHOT.jar
```

For complete deployment steps, environment variables, and K8s template usage, see **[deploy.md](./deploy.md)**.

## Documentation

- **[API.md](./API.md)** — HTTP/WebSocket APIs, request body fields, inter-service call documentation.
- **[deploy.md](./deploy.md)** — Runtime conventions, environment variables, Maven/Docker/Kubernetes deployment instructions.