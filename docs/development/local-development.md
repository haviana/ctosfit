# Local development

## Requirements
- JDK 21
- Gradle 8.14.1 or a compatible Gradle 8.x release

The Gradle Wrapper is not included in this initial bootstrap. Use a locally installed Gradle; adding and verifying the wrapper is a follow-up hardening task.

## Commands
```bash
gradle clean test
gradle build
gradle :app:bootRun
gradle :app:bootJar
```

Health endpoint: http://localhost:8080/actuator/health

Import as a Gradle project and select JDK 21 as the Gradle JVM and project toolchain.
