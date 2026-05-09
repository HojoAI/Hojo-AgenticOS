# hojoai-parent

HoJoAI Java project's **root BOM (Bill of Materials)**: extends `spring-boot-starter-parent`, centrally locks Spring Cloud, gRPC, common third-party libraries, and `hojoai-common` versions in this repo.

## Responsibilities

- `dependencyManagement`: imports Spring Cloud BOM, gRPC BOM, Protobuf BOM, and project-wide version properties.
- Conventions: **Java 21**, **UTF-8** encoding, compiler and source plugin versions, etc.

## Usage

Business modules use this as `<parent>` in `pom.xml`, no need to redeclare dependency versions already managed by this BOM:

```xml
<parent>
    <groupId>com.hojoai.parent</groupId>
    <artifactId>hojoai-parent</artifactId>
    <version>3.25.10</version>
</parent>
```

The specific version number is based on the current `pom.xml`.

## Notes

- This project's **`packaging` is `pom`**, cannot run `spring-boot:run` alone.
- The authoritative source for version and dependency matrix is the `pom.xml` in this directory.