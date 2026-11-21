# CTOSFit — Claude Code instructions

Read before changing code. These rules apply to all developers and coding agents.

## Product
Workout-first CrossFit / Cross Training platform. Current foundation: Kotlin/Spring Boot modular monolith with identity, box, exercise, programming, results and performance modules. Do not invent unspecified product behavior.

## Mandatory architecture
- Use Hexagonal Architecture (Ports and Adapters) in each business module.
- Domain and application code must not depend on Spring, HTTP, databases, messaging or vendor SDKs.
- Inbound use-case ports: `application/port/in`; outbound dependency ports: `application/port/out`.
- Use-case orchestration: `application/service`; adapters: `adapter/in` and `adapter/out`.
- Dependencies point inward. Adapters implement ports.
- `app` is the composition root. Business modules must never depend on `app`.

## Interfaces, abstract classes and reuse
- Prefer small, intention-revealing interfaces for ports and contracts.
- Use abstract classes only when multiple implementations share a stable algorithm or invariant that cannot be expressed more clearly with composition.
- Do not add inheritance merely to satisfy a pattern. Prefer composition and delegation.
- Do not duplicate validation, mapping, policies or orchestration. Extract shared behavior only when a real common concept has a clear owner.
- Do not create speculative generic frameworks for one implementation.

## Module boundaries
- Each business module owns its domain, use cases and adapters.
- No module may access another module's repositories, tables, persistence entities or internal services.
- Avoid business-module-to-business-module Gradle dependencies. Cross-module collaboration requires an explicit public application contract and an ADR describing ownership and dependency direction.
- The app module may depend on all business modules.
- Avoid a dumping-ground shared/common module.

## Gradle dependency management
- Declare plugin and external library versions only in `gradle/libs.versions.toml`.
- Use version-catalog aliases (`libs.*`) in every module build script; never hardcode dependency versions there.
- Keep repositories centralized in `settings.gradle.kts`.
- Import the Spring Boot BOM in each module that consumes Spring Boot-managed dependencies.
- Update the dependency-management documentation when the catalog policy changes.

## Kotlin and Spring
- Kotlin official style, immutable values by default, constructor injection.
- No field injection or service locator.
- Controllers translate transport data and call inbound ports; they contain no business rules.
- Explicit types on public APIs. Nullable types only when absence is part of the contract.
- Keep Spring annotations out of domain types.

## Testing
- Domain unit tests without Spring.
- Application tests using port fakes.
- Adapter tests at boundaries.
- Deterministic tests; no external services in unit tests.
- Every behavior change requires appropriate tests.

## Documentation (required)
- Update the owning module README when responsibilities, public ports or dependencies change.
- Record significant design choices in `docs/decisions/` as ADRs.
- Keep setup and commands accurate.
- Document intent, constraints and trade-offs, not obvious syntax.

## Workflow
1. Read relevant architecture docs and module README.
2. Identify owning module and dependency direction.
3. Define/update port before adapter.
4. Implement the smallest complete vertical slice.
5. Add tests and update docs.
6. Run `gradle clean test` and `gradle build`.
7. Summarize files, decisions, tests and limitations.

## Commands
`gradle clean test` · `gradle build` · `gradle :app:bootRun` · `gradle :app:bootJar`

## Out of scope unless requested
Microservices, distributed transactions, premature event-driven architecture, authentication-provider integration, database schema and migrations, speculative multi-tenancy enforcement.
