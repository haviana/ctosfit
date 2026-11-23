# ADR 0003: Adopt Java 25 and Spring Boot 4.2

- Status: Accepted
- Date: 2026-11-21

## Context

CTOSFit is a new project and has no production compatibility constraints. Starting on an older platform generation would create avoidable upgrade work. Java 25 is an LTS release, and Spring Boot 4.2 is the latest stable Spring Boot generation at the time of this decision.

## Decision

Use the following baseline:
- Java 25 LTS as the compile and runtime toolchain.
- Spring Boot 4.2.0.
- Kotlin 2.4.21 (Spring Boot 4 requires Kotlin 2.2 or later).
- Gradle 9.7.0.

Gradle 9.7.0 is selected because Java 25 is supported when running Gradle from 9.1.0 onward, while Kotlin Gradle Plugin 2.4.21 lists full compatibility through Gradle 9.7.0. This intentionally prioritizes a documented compatible toolchain over using the newest Gradle patch immediately.

Dependency and plugin versions remain centralized in `gradle/libs.versions.toml`.

## Consequences

- CI and local development use JDK 25.
- Spring Boot 4's Spring Framework 7 baseline and Jakarta EE 11 / Servlet 6.1 ecosystem apply.
- Spring Boot 4.2 deprecates `RestTemplate` and `TestRestTemplate` support in favor of `RestClient` and `RestTestClient`; new code should use the newer clients.
- Kotlin and Gradle upgrades must be checked together against their compatibility matrix.
- Revisit the Gradle version when Kotlin Gradle Plugin's fully supported range advances.
