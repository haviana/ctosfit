# Architecture Overview

## Direction
Start with a **modular monolith**. The product is early, the team is small and domain boundaries are still being validated. A distributed system would add cost without demonstrated need.

## Initial technology direction
- Backend: Kotlin, Spring Boot, Gradle Kotlin DSL.
- Frontend: React, TypeScript, Tailwind CSS and shadcn/ui.
- Database: PostgreSQL.
- Media: S3-compatible object storage.
- Authentication: OIDC/OAuth2 integration with Spring Security.
- API: REST/OpenAPI.
- Local development: Docker Compose.
- CI: GitHub Actions.
- Observability: structured logs, metrics and tracing with OpenTelemetry-compatible instrumentation; evaluate Sentry for error tracking.

Select exact framework/runtime versions at implementation start, verify compatibility/support, and record them in build files and an ADR. No runtime version is assumed by this document.

## Modules
- **Identity:** users, authentication integration, memberships and roles.
- **Box:** tenant profile, configuration and settings.
- **Exercise:** exercise catalogue and movement metadata.
- **Programming:** workouts, blocks, templates, schedules and publication snapshots.
- **Results:** athlete submissions and typed scores.
- **Performance:** personal records, benchmarks and derived insights.
- **Notifications:** later delivery preferences/events.
- **Shared:** narrowly scoped technical primitives, not a dumping ground.

Each module owns its domain logic and persistence access. Cross-module interaction uses explicit application interfaces or in-process events, not another module's internal repositories.

## Hexagonal boundaries
Inbound adapters (REST/controllers/jobs) call application use cases. Application orchestrates domain objects and ports. Domain contains business rules and avoids Spring, JPA and HTTP dependencies. Outbound adapters implement ports for PostgreSQL, object storage, identity providers and external services.

Use interfaces where they establish meaningful boundaries, enable substitution/testing or protect domain/application code from infrastructure. Avoid unnecessary abstractions and one-line wrappers.

## Multi-tenancy
Initial approach: shared PostgreSQL database/schema with tenant_id on tenant-owned records.
- Resolve tenant context from authenticated membership and requested box context.
- Pass trusted tenant context into use cases.
- Scope every tenant-owned repository operation.
- Never treat client-supplied tenant ID as authorization.
- Include tenant scope in uniqueness constraints where needed.
- Test cross-tenant reads, writes, lists, nested resources and identifier guessing.
- Audit membership and role changes.

The verified model, denial rules, and PostgreSQL test approach are in [tenant isolation](tenant-isolation.md).

PostgreSQL Row-Level Security may be evaluated as defense in depth, considering connection-pool/session-context behavior and migration complexity. It does not replace application authorization tests.

## Data integrity
Use versioned database migrations. Keep editable drafts separate from immutable published snapshots. A publication stores the exact prescribed workout; results reference the publication, not a mutable draft. Use explicit score types and units. Wrap multi-record state transitions such as publication in transactions.

## API and frontend
Design REST endpoints around application capabilities, not direct table CRUD. Maintain an OpenAPI contract. Validate input at boundaries and enforce invariants in domain/application layers. Frontend routes reflect admin/coach/athlete capabilities; hiding a UI control is never a security boundary.

## Testing
Domain tests for workout validation, publication and score comparison; application tests for use cases; PostgreSQL integration tests; tenant-isolation tests; API auth/validation tests; end-to-end coach publication and athlete result logging. The tenant-isolation suites use Testcontainers against PostgreSQL and are described in [tenant isolation](tenant-isolation.md).

## Deployment
Start with one backend deployment, one frontend deployment and managed PostgreSQL. Keep object storage separate. Configure environments and secrets securely. Define health checks, logs, metrics, alerting, backup retention and restore before pilot.

Do not introduce microservices, Kafka, Kubernetes, event sourcing or CQRS by default. Revisit only when concrete scale, integration, team ownership or reliability needs justify the cost.