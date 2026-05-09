# Local Build Guide (Maven)

This repository has **no** root aggregate `pom.xml`. Execute `mvn clean install -DskipTests` (or `mvn install`) in each subdirectory to install artifacts to local `~/.m2`.

Build **does not depend** on private Maven artifact repositories (e.g., the removed `repo-ppkyi`); dependencies should come from **Maven Central** or **modules already installed from source on the local machine**.

## Prerequisite: `hojoai-omniasst-common`

Source code is in the repository directory **`hojoai-omniasst-common/`**, parent POM is **`hojoai-parent`**, and the parent project must be installed to local `~/.m2` before building.

The following modules' `*-server` depend on `com.hojoai.omniasst.common:hojoai-omniasst-common` (version based on each module's `pom.xml`):

- `hojoai-webmvc-omniasst-management`
- `hojoai-webmvc-omniasst-focus`
- `hojoai-webmvc-omniasst-translate`
- `hojoai-websocket-omniasst-sysagent`
- `hojoai-webmvc-omniasst-meeting`

If the `jar` is not yet installed locally, execute in the **repository root** (step 1 below must be completed first, step 2 if necessary):

```bash
mvn -f hojoai-omniasst-common/pom.xml clean install -DskipTests
```

Confirm the corresponding version exists under `~/.m2/repository/com/hojoai/omniasst/common/hojoai-omniasst-common/` before continuing.

## Recommended Installation Order (In-Repository Modules)

Execute in sequence from the repository root (paths relative to repository root):

```bash
# 1. Parent BOM (parent of all other modules)
mvn -f hojoai-parent/pom.xml clean install -DskipTests

# 2. Common library
mvn -f hojoai-common/pom.xml clean install -DskipTests

# 2b. Omniasst domain common library (dependency of multiple omniasst *-server; only depends on hojoai-parent)
mvn -f hojoai-omniasst-common/pom.xml clean install -DskipTests

# 3. Central gRPC (includes api + server)
mvn -f hojoai-grpc-central-provider/pom.xml clean install -DskipTests

# 4. Gateways and business not depending on hojoai-omniasst-common (can be trimmed as needed)
mvn -f hojoai-gateway-omniasst/pom.xml clean install -DskipTests
mvn -f hojoai-gateway-omniasst-voice/pom.xml clean install -DskipTests
mvn -f hojoai-gateway-central/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-central-account/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-account/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-music/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-weather/pom.xml clean install -DskipTests

# 5. Modules depending on hojoai-omniasst-common (step 2b or the above "Prerequisite" install must be completed first)
mvn -f hojoai-webmvc-omniasst-management/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-translate/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-focus/pom.xml clean install -DskipTests
mvn -f hojoai-websocket-omniasst-sysagent/pom.xml clean install -DskipTests
mvn -f hojoai-webmvc-omniasst-meeting/pom.xml clean install -DskipTests

# 6. Template (optional)
mvn -f hojoai-template/pom.xml clean install -DskipTests
```

If an error reports missing dependencies, go back to the corresponding module directory based on the `groupId:artifactId` in the error and execute `install` first.

## Relationship with CI

The repository's [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) runs continuous integration for modules **not depending** on `hojoai-omniasst-common`; modules depending on this library need to have dependencies satisfied locally or via a supplementary artifact source before being included in the same pipeline.
