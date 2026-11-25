# Local development

## Requirements
- JDK 25
- Gradle 9.7.0
- Docker Compose

Gradle 9.1.0 or later is required to run Gradle on Java 25. Gradle 9.7.0 is selected to stay within the fully supported range documented for Kotlin Gradle Plugin 2.4.21.

The repository includes the Gradle Wrapper, pinned to Gradle 9.7.0. No system Gradle installation is needed.

## Commands
```bash
docker compose up -d postgres
./gradlew clean test
./gradlew build
./gradlew :app:bootRun
./gradlew :app:bootJar
```

The app connects to the local PostgreSQL instance and applies versioned Flyway
migrations on startup. Set `CTOSFIT_DATABASE_URL`, `CTOSFIT_DATABASE_USERNAME`,
`CTOSFIT_DATABASE_PASSWORD`, and `CTOSFIT_WEB_ALLOWED_ORIGINS` as needed. Origins are
comma-separated; the default allows Vite at
`http://localhost:5173`. The credentials in
`docker-compose.yml` are for local development only.

Configure `CTOSFIT_SECURITY_ISSUER_URI`, `CTOSFIT_SECURITY_JWK_SET_URI`, and
`CTOSFIT_SECURITY_AUDIENCE` for the OIDC provider. The API defaults to the audience
`ctosfit-api`; tokens from the configured issuer must include that audience. Roles are
resolved from active box memberships on each request, so the provider does not need a
global application-role claim.

Health endpoint: http://localhost:8080/actuator/health

Import as a Gradle project and select JDK 25 as the Gradle JVM and project toolchain.
