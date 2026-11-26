# Local development

## Requirements
- JDK 25

The repository includes the Gradle Wrapper, pinned to Gradle 9.7.0. No system Gradle installation is needed. Gradle 9.1.0 or later is required to run on Java 25; 9.7.0 stays within the supported range documented for Kotlin Gradle Plugin 2.4.21.

## Commands
```bash
./gradlew clean test
./gradlew build
SPRING_PROFILES_ACTIVE=local ./gradlew :app:bootRun
./gradlew :app:bootJar
```

Health endpoint: http://localhost:8080/actuator/health

The `local` profile loads `app/src/main/resources/application-local.properties` and uses the PostgreSQL instance from `docker-compose.yml`.

Import as a Gradle project and select JDK 25 as the Gradle JVM and project toolchain.
