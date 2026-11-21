# Architecture Standard

- Follow the architecture overview and accepted ADRs.
- A module owns its domain model, application use cases and persistence implementation.
- Cross-module access goes through explicit application contracts or in-process events.
- Controllers/jobs are inbound adapters; repositories/external clients are outbound adapters.
- Keep domain free of Spring/JPA/HTTP dependencies.
- Use transactions around business state transitions, not arbitrary controller methods.
- Published workouts are immutable versioned snapshots; results point to a snapshot.
- Shared is for small technical primitives, not a shared domain dumping ground.
- New infrastructure or architectural patterns require evidence and an ADR.
