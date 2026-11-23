# Ctosfit

> Working name for a workout-programming and athlete-performance platform for CrossFit and functional-fitness boxes. The name is provisional and may be changed later.

## Product direction

Ctosfit is a workout-first SaaS. The initial goal is to help coaches create, structure, schedule and publish workouts, while athletes can view sessions, log typed results and review progress.

The first release is deliberately not a complete gym-management system. Billing, class booking, access control, native apps and wearable integrations are out of scope for the MVP.

## Documentation

- [Product vision and scope](docs/product/vision-and-scope.md)
- [MVP requirements](docs/product/mvp-requirements.md)
- [Product roadmap](docs/product/roadmap.md)
- [Architecture overview](docs/architecture/overview.md)
- [Initial domain model](docs/architecture/domain-model.md)
- [ADR 0001 — Modular monolith](docs/engineering/decisions/0001-modular-monolith.md)
- [Discovery and pilot plan](docs/operations/discovery-and-pilot.md)

## Initial technical direction

- Backend: Kotlin + Spring Boot + Gradle Kotlin DSL
- Frontend: React + TypeScript + Tailwind CSS + shadcn/ui
- Database: PostgreSQL
- API: REST + OpenAPI
- Authentication: OIDC/OAuth2 with Spring Security
- Delivery: Docker-based local development and GitHub Actions

The initial compatible runtime and framework baseline is recorded with the build. Review supported versions when upgrading dependencies.

## Backend quick start

The initial backend is a Gradle multi-project build. It contains a Spring Boot `app` plus six Kotlin library modules. Java 21, Kotlin 2.2.21, Spring Boot 3.5.16 and Gradle 8.14.3 are pinned as the initial compatible baseline.

With a Java 21 JDK available:

```sh
./gradlew :app:bootRun
```

Then open `http://localhost:8080/`; the starter endpoint returns `{"service":"ctosfit","status":"running"}`. Build all modules with `./gradlew build`.

### Module boundaries

See [backend module boundaries](docs/architecture/backend-modules.md) for ownership and dependency rules.

## Engineering principles

- Modular monolith with explicit hexagonal boundaries.
- Domain logic independent of delivery and persistence frameworks where practical.
- Multi-tenancy enforced server-side and covered by cross-tenant tests.
- Structured workouts and typed results, not free-text-only records.
- Immutable published workout snapshots.
- Coach approval for AI-generated or AI-modified programming.
- Prefer the simplest infrastructure that safely supports validated product needs.

## Project management

The initial product backlog is tracked in [Jira](https://hugohaviana.atlassian.net/jira/software/projects/WOD/boards/1).

## Reusable engineering standards

This repository should consume and reuse the agents, skills, workflows and engineering standards maintained in [ai-development-umbrella](https://github.com/haviana/ai-development-umbrella), rather than duplicating the umbrella's reusable assets here.

## Current status

Documentation and initial scope are established. Product discovery and validation should precede committing to the full MVP implementation.
