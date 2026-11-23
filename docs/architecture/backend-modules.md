# Backend module boundaries

The Gradle build is a modular monolith: `app` is the single Spring Boot entry point and the six domain modules are Kotlin/JVM libraries. The app depends on each module; domain modules do not depend on the app or on one another. This keeps module ownership explicit while the first capabilities are bootstrapped.

| Gradle project | Owns | Boundary guidance |
| --- | --- | --- |
| `:identity` | Users, authentication integration, box memberships and roles | Own authentication and membership rules; do not depend on box internals. |
| `:box` | Box profile, configuration and tenant settings | Own tenant identity/configuration; authorization must use trusted membership context. |
| `:exercise` | Exercise catalogue and movement metadata | Own exercise definitions and archive semantics; expose application interfaces for consumers. |
| `:programming` | Workout drafts, templates, scheduling and immutable publication snapshots | Own authoring and publication rules; refer to exercise data through public contracts. |
| `:results` | Athlete submissions and typed scores | Own result validation and score semantics; refer to publications through stable contracts. |
| `:performance` | Benchmarks, comparable metrics and derived personal records | Own derivation and comparison rules; source results remain authoritative. |

## Dependency rules

- `app` may assemble modules and provide Spring, HTTP and persistence adapters.
- Domain and application code belongs in the named module, not in `app`.
- A module must not import another module's internal package or repository. Cross-module calls use an explicitly published application interface or event.
- Domain rules should remain independent of Spring, HTTP and persistence frameworks where practical.
- Shared primitives require a specific cross-module use case; avoid creating a general-purpose dumping ground.

The initial modules are deliberately dependency-free Kotlin libraries. Persistence, authentication, API endpoints and module-to-module contracts will be introduced with their owning capabilities in later work.
