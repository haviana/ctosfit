# Hexagonal Architecture (Ports and Adapters)

## Goal
Protect business rules from infrastructure. Domain and application code must remain usable without Spring, databases, HTTP, message brokers or vendor SDKs.

## Layers

| Layer | Responsibility | May depend on |
|---|---|---|
| Domain | Entities, value objects, domain services and invariants | Kotlin/JDK and domain types in the same module |
| Application | Use-case orchestration and transaction boundaries | Domain and ports |
| Inbound ports | Use cases offered by the module | Application/domain types |
| Outbound ports | Capabilities required by use cases | Domain/application types |
| Inbound adapters | HTTP, messaging, scheduled jobs, CLI | Inbound ports and transport libraries |
| Outbound adapters | Persistence, external APIs, messaging clients | Outbound ports and infrastructure libraries |

## Dependency rule

```text
Inbound adapter -> Inbound port -> Application service -> Domain
                                             |
                                             v
                                        Outbound port
                                             ^
                                             |
                                       Outbound adapter
```

An application service calls an outbound port; an infrastructure adapter implements it. Source dependencies always point inward.

## Ports and adapters
- Inbound ports express use cases in business language (for example, `CreateWorkout`).
- Outbound ports express required capabilities (for example, `WorkoutRepository`).
- Keep interfaces small and owned by the consumer that needs them.
- Never leak JPA entities, Spring types, HTTP request objects or vendor DTOs through ports.
- Adapters translate external protocols and technologies; they do not own business decisions.

## Interfaces and abstract classes
Use interfaces for ports and replaceable capabilities. Prefer composition for shared implementation. An abstract class is justified only when multiple implementations share a stable algorithm or invariant with meaningful extension points. Do not add a base class for one implementation or merely to avoid a few lines of duplication.

## Transactions and cross-cutting concerns
Transaction boundaries belong in application orchestration or an application-facing abstraction. Spring-specific transaction configuration belongs in adapters/configuration, not domain types. Logging, metrics and tracing belong at application or adapter boundaries.

## Suggested module layout

```text
<module>/
  src/main/kotlin/com/ctosfit/<module>/
    domain/
    application/
      port/in/
      port/out/
      service/
    adapter/
      in/
      out/
  README.md
```

Create packages when real types are introduced; do not add empty placeholder classes.
