# Ctosfit

> Working name for a workout-programming and athlete-performance platform for CrossFit and functional-fitness boxes. The name is provisional and may be changed later.

Ctosfit is a workout-first SaaS. The initial goal is to help coaches create, structure, schedule and publish workouts, while athletes can view sessions, log typed results and review progress.

The first release is deliberately not a complete gym-management system. Billing, class booking, access control, native apps and wearable integrations are out of scope for the MVP.

## Product documentation

- [Product vision and scope](docs/product/vision-and-scope.md)
- [MVP requirements](docs/product/mvp-requirements.md)
- [Product roadmap](docs/product/roadmap.md)
- [Architecture overview](docs/architecture/overview.md)
- [Initial domain model](docs/architecture/domain-model.md)
- [Discovery and pilot plan](docs/operations/discovery-and-pilot.md)
- [REST API contract](docs/api/openapi.yaml)

## Initial technical direction

- Backend: Kotlin + Spring Boot + Gradle Kotlin DSL
- Frontend: React + TypeScript + Tailwind CSS + shadcn/ui
- Database: PostgreSQL
- API: REST + OpenAPI
- Authentication: OIDC/OAuth2 with Spring Security
- Delivery: Docker-based local development and GitHub Actions

## Backend foundation

The backend starts as a Kotlin + Spring Boot **modular monolith** with Hexagonal Architecture inside each business module.

### Modules

- `identity` — identity and access
- `box` — gym / training facility context
- `exercise` — exercise catalog
- `programming` — workout and training programming
- `results` — recorded workout outcomes
- `performance` — metrics and performance views
- `app` — Spring Boot composition root

### Platform baseline

Java 25 LTS · Kotlin 2.4.21 · Spring Boot 4.2.0 · Gradle 9.7.0 · Gradle Kotlin DSL · JUnit 5

All plugin and library versions are centralized in [gradle/libs.versions.toml](gradle/libs.versions.toml). Module build files use version-catalog aliases; do not hardcode dependency versions in module scripts. See [dependency management](docs/development/dependency-management.md).

### Run locally

Requirements: JDK 25, Gradle 9.7.0 and Docker Compose.

```bash
docker compose up -d postgres
gradle clean test
gradle :app:bootRun
```

Health endpoint: http://localhost:8080/actuator/health

Build: `gradle build` · Jar: `gradle :app:bootJar`

### Web preview

Requirements: Node.js and npm.

```bash
cd web
npm install
npm run dev
```

The web preview includes a public product landing page and a role-based interactive demo
for box owners, coaches and athletes. Demo actions use fictional sample data in the
browser and do not call a live API or access tenant data.

Configure the authenticated OIDC workspace using the [web app setup guide](web/README.md).

### Architecture

Dependencies point inward: inbound adapters call inbound ports; application services orchestrate domain behavior and outbound ports; outbound adapters implement those ports. Domain and application code must not depend on Spring, HTTP, databases or vendor SDKs.

Read [CLAUDE.md](CLAUDE.md) before making backend changes. See [Hexagonal Architecture](docs/architecture/hexagonal-architecture.md), [module boundaries](docs/architecture/module-boundaries.md), [development guide](docs/architecture/development-guide.md), [local development](docs/development/local-development.md), [dependency management](docs/development/dependency-management.md), and [ADRs](docs/decisions/).

## Engineering principles

- Modular monolith with explicit hexagonal boundaries.
- Domain logic independent of delivery and persistence frameworks where practical.
- Multi-tenancy enforced server-side and covered by cross-tenant tests.
- Structured workouts and typed results, not free-text-only records.
- Immutable published workout snapshots.
- Coach approval for AI-generated or AI-modified programming.
- Prefer the simplest infrastructure that safely supports validated product needs.
- Prefer interfaces for meaningful ports and contracts; use abstract classes only for proven shared algorithms or invariants. Prefer composition and avoid duplicated business rules.

## Project management

The initial product backlog is tracked in [Jira](https://hugohaviana.atlassian.net/jira/software/projects/WOD/boards/1).

## Reusable engineering standards

This repository consumes a curated, Ctosfit-adapted snapshot of the agents, skills, standards, governance and workflows maintained in [AI Development Umbrella](https://github.com/haviana/ai-development-umbrella). Start with [AGENTS.md](AGENTS.md), browse [agents](agents/README.md), [skills](skills/README.md), [standards](standards/engineering-principles.md) and [governance](governance/approval-workflow.md). See [UPSTREAM.md](UPSTREAM.md) for adoption and update policy.

## Current status

The app now has box onboarding and owner-managed memberships, JWT-authenticated tenant-scoped REST and PostgreSQL persistence for exercises, workout drafts/publications and typed results, and a responsive authenticated browser workspace. The public landing page and sample-data demo remain isolated from live tenant data. Memberships currently require an existing identity-provider user ID; invitation delivery, broader verified PostgreSQL tenant-isolation coverage, and a demonstrated backup restore remain before full MVP acceptance.
