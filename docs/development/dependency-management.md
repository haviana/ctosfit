# Gradle dependency management

## Single source of truth

All external plugin and library versions are declared in `gradle/libs.versions.toml`. Gradle automatically imports this file as the `libs` version catalog.

The catalog manages:
- Plugin versions (Kotlin JVM, Kotlin Spring and Spring Boot).
- Library versions (Kotlin, JUnit and Spring Boot).
- Dependency coordinates and aliases for Spring Boot starters and Jackson.

Module build files consume aliases such as `libs.spring.boot.starter.web`; they must not repeat external dependency coordinates or versions.

## Version updates

1. Change the relevant version in `[versions]` in `gradle/libs.versions.toml`.
2. Keep related plugin and library aliases linked through `version.ref`.
3. Review Spring Boot compatibility and release notes before upgrading its version.
4. Run `gradle clean test build`.
5. Review dependency changes and commit the catalog and build-script changes together.

## Spring Boot BOM

Spring Boot starters and Jackson are intentionally declared without individual versions. The consuming module imports `libs.spring.boot.dependencies` using Gradle's `platform(...)`, so the Spring Boot BOM selects a compatible set of transitive versions.

Each module that directly consumes Spring Boot-managed libraries must import the BOM in its own dependency configuration. Do not rely on an application module's platform to manage dependencies in another Gradle project.

## Internal modules versus external libraries

- External dependencies are centrally versioned in the version catalog.
- Internal business modules are connected with Gradle project dependencies.
- Do not create a shared business module just to centralize third-party libraries.
- Do not add a generic `common` module as a dumping ground.
- If real reusable technical build logic emerges, introduce a convention plugin (for example in `build-logic`) rather than copy-pasting build configuration.

## Repository policy

Repositories are declared centrally in `settings.gradle.kts`. Project-level repository declarations are rejected to keep dependency resolution consistent.
