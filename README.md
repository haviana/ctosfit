# CTOSFit

Workout-first CrossFit / Cross Training platform. This repository starts as a Kotlin + Spring Boot **modular monolith** with Hexagonal Architecture inside each business module.

## Modules
- `identity` — identity and access
- `box` — gym / training facility context
- `exercise` — exercise catalog
- `programming` — workout and training programming
- `results` — recorded workout outcomes
- `performance` — metrics and performance views
- `app` — Spring Boot composition root

## Stack
Java 21 · Kotlin 2.1.20 · Spring Boot 3.5.0 · Gradle Kotlin DSL · JUnit 5

## Run
Requirements: JDK 21 and Gradle 8.14.1 or compatible.
```bash
gradle clean test
gradle :app:bootRun
```
Health: http://localhost:8080/actuator/health

Build: `gradle build` · Jar: `gradle :app:bootJar`

## Architecture
Dependencies point inward: inbound adapters call inbound ports; application services orchestrate domain behavior and outbound ports; outbound adapters implement those ports. Domain and application code must not depend on Spring, HTTP, databases or vendor SDKs.

Read [CLAUDE.md](CLAUDE.md) before making changes. See [architecture](docs/architecture/hexagonal-architecture.md), [module boundaries](docs/architecture/module-boundaries.md), [development guide](docs/development/local-development.md), and [ADRs](docs/decisions/).

This bootstrap deliberately does not invent product behavior, authentication flows, database schemas or API contracts.
