# ADR 0001: Start with a modular monolith

- Status: Accepted
- Date: 2026-11-21

## Context
The product is an early-stage solo-founder side project. Independent deployments and distributed infrastructure would add operational cost before module boundaries and domain behavior are understood.

## Decision
Start with one Spring Boot deployable application and independently buildable Gradle modules for identity, box, exercise, programming, results and performance.

## Consequences
- One local startup and one deployment unit.
- Module boundaries are enforced through Gradle project dependencies and code review.
- Business modules must not depend on each other by default.
- Modules can be extracted later if scaling, ownership or deployment needs justify it.
- These modules are not microservices.
