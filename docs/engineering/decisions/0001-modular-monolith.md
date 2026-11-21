# ADR 0001: Modular Monolith and Hexagonal Boundaries

- **Status:** Accepted as initial direction
- **Date:** 2026-11-21
- **Decision owner:** Product founder / engineering owner

## Context
The product is early-stage, built by a very small team. Domain boundaries are being validated. The first release focuses on programming, athlete results and basic performance. Operational simplicity and rapid feedback outweigh independent service deployment.

## Decision
Build one deployable modular monolith with explicit domain, application and infrastructure boundaries. Use Kotlin/Spring Boot, Gradle Kotlin DSL, React/TypeScript and PostgreSQL.

Domain contains business rules. Application contains use cases and ports. Adapters implement inbound/outbound integrations. Framework dependencies remain outside domain code where practical. Modules communicate through explicit interfaces or in-process events.

## Consequences
**Benefits:** simple local development/deployment, easier early transactions, lower operational overhead and a path to extract services if justified.

**Trade-offs:** boundaries need discipline and architecture tests; modules share release cadence and runtime failure domain; poor enforcement can produce a tangled monolith.

## Rejected alternatives
- **Microservices:** no demonstrated independent scaling, ownership or deployment requirement.
- **Distributed event-driven architecture:** unnecessary operational complexity at MVP stage.
- **Unstructured layered monolith:** weaker domain boundaries and harder evolution.

## Revisit when
A module has independently justified scaling, availability, integration or team-ownership requirements, and extraction costs less than keeping it in the monolith.