# Local development

## Requirements
- JDK 25
- Gradle 9.7.0

Gradle 9.1.0 or later is required to run Gradle on Java 25. Gradle 9.7.0 is selected to stay within the fully supported range documented for Kotlin Gradle Plugin 2.4.21.

The Gradle Wrapper is not included yet. Until it is added, use a locally installed Gradle 9.7.0.

## Commands
```bash
gradle clean test
gradle build
gradle :app:bootRun
gradle :app:bootJar
```

Health endpoint: http://localhost:8080/actuator/health

Import as a Gradle project and select JDK 25 as the Gradle JVM and project toolchain.
