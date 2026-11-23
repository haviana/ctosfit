# ADR 0002: Use Hexagonal Architecture inside business modules

- Status: Accepted
- Date: 2026-11-21

## Context
Business rules should remain testable and independent of Spring Boot, persistence technology and external providers.

## Decision
Each business module uses ports and adapters: inbound ports expose use cases; outbound ports describe required capabilities; application services orchestrate; domain types enforce invariants; adapters translate external protocols and technologies.

Interfaces are the default for ports. Abstract classes are allowed only for proven shared algorithms or invariants; composition is preferred.

## Consequences
- Domain and application code can be unit-tested without Spring.
- Infrastructure can be replaced behind outbound ports.
- Dependency direction must remain explicit.
- Avoid speculative ports and abstractions until a use case requires them.
