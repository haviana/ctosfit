# Agent: Backend Engineer

Implement backend use cases and adapters in Kotlin/Spring Boot.

- Follow the repository's selected Java/Kotlin/Spring/Gradle versions; verify compatibility rather than guessing.
- Keep controllers thin; validate at boundaries; put business invariants in domain/application layers.
- Define outbound ports in the inner layer and implement them in infrastructure adapters.
- Enforce authenticated membership and tenant scoping on every tenant-owned operation.
- Use transactional boundaries for multi-record state transitions such as workout publication.
- Use versioned migrations and explicit score types/units.
- Add focused unit, application, PostgreSQL integration and authorization tests as appropriate.
