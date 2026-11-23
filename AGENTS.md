# Ctosfit — Agent Instructions

## Product context
Ctosfit is a workout-first SaaS for CrossFit and functional-fitness boxes. Coaches create, structure, schedule and publish workouts; athletes view published sessions, submit typed results and review progress. The MVP is not a full gym-management suite.

Read [README.md](README.md), [architecture overview](docs/architecture/overview.md), product requirements and relevant ADRs before making material changes.

## Engineering defaults
- Backend direction: Kotlin + Spring Boot + Gradle Kotlin DSL. Frontend direction: React + TypeScript. Persistence: PostgreSQL. API: REST/OpenAPI.
- Use a modular monolith with explicit hexagonal boundaries.
- Domain rules must not depend on Spring, JPA, HTTP or delivery concerns.
- Modules own their domain and persistence. Cross-module collaboration uses explicit application interfaces or in-process events; do not reach into another module's repositories.
- Introduce interfaces when they define a meaningful port, substitution seam or test boundary. Avoid ceremonial interfaces, speculative abstractions and duplicate wrappers.
- Keep the simplest design that meets validated requirements. Do not introduce microservices, Kafka, Kubernetes, CQRS or event sourcing without evidence and an ADR.

## Non-negotiable product invariants
- Resolve tenant scope from authenticated membership and trusted application context. A client-supplied tenant/box ID is never authorization.
- Scope every tenant-owned read and write; test cross-tenant access, including nested resources and guessed identifiers.
- Keep editable workout drafts separate from immutable published snapshots. Athlete results must reference the exact published version.
- Represent workout scores, units and score types explicitly; do not reduce structured results to unvalidated free text.
- AI-generated or AI-modified programming must be reviewed and approved by a coach before publication.
- Enforce permissions on the server; frontend visibility is not a security boundary.

## Change workflow
1. Inspect existing code, docs, conventions and tests before editing.
2. State assumptions and produce a small implementation plan for non-trivial work.
3. Prefer focused, reversible changes. Update docs and ADRs when architecture or product decisions change.
4. Add or update tests at the correct layer; include tenant-isolation and authorization tests when relevant.
5. Run the available checks and report exact commands/results. Never claim a check passed if it was not run.
6. Keep secrets, credentials, personal data and production data out of logs, fixtures and commits.
7. Use conventional commit messages and a PR with scope, rationale, tests, risks and migration/rollback notes.

## Agents and skills
- Agent role definitions live in [agents/](agents/).
- Reusable task playbooks live in [skills/](skills/).
- Project-specific standards live in [standards/](standards/).
- Human approval and quality gates are defined in [governance/](governance/).
- Upstream source and adoption notes are tracked in [UPSTREAM.md](UPSTREAM.md).

### Automatic specialist routing
The orchestrator selects and invokes applicable specialist roles as part of the task workflow; users do not need to name agents explicitly. Apply these triggers:

- **Software Architect:** new project scaffolds; module or service boundaries; framework, platform or data-contract decisions; material architecture changes.
- **Test Engineer:** behavior changes, bug fixes, or acceptance criteria that need observable verification. Ask for a risk-based test plan before implementation and review the resulting verification evidence afterward.
- **Code Reviewer:** every non-trivial code or configuration change, after implementation and before reporting completion. This review must be independent of the implementer. Run the local reviewer when GitHub Actions review is unavailable, skipped, or only advisory.
- **Security Engineer:** authentication, authorization, tenant isolation, secrets, sensitive data, dependencies with security impact, or CI permissions.
- **DevOps Engineer:** build, CI/CD, deployment, infrastructure, runtime configuration, or operational reliability changes.
- **Performance Engineer:** explicit latency, throughput, resource, or scale requirements; a measured bottleneck; or a change with material performance risk. Require a workload and evidence rather than speculative optimization.
- **Documentation Agent:** user-facing setup, architecture, API, configuration, runbook, or release documentation changes.
- **Product Analyst:** product scope, user journeys, or acceptance criteria are unclear or conflict with the stated MVP.

For non-trivial feature work, invoke the architect and test engineer when their triggers apply, then use the code reviewer after implementation. Specialists advise within their role; the orchestrator integrates their findings and remains responsible for the final report. Do not claim a role ran unless it was actually invoked.

### Jira lifecycle
For a task linked to a Jira issue:

1. Read the issue before implementation and use its acceptance criteria as the work scope.
2. When work actually starts, move the issue to **In Progress** using an available Jira workflow transition. Confirm the resulting status; do not infer it from the requested transition name.
3. Complete implementation, specialist reviews, local verification and PR creation while the issue remains In Progress.
4. Move the issue to **Done** only after GitHub confirms the PR was merged into its target branch (`main` or `master`). PR creation, approval, passing checks or a request to merge do not mean the work is done.
5. After the merge, transition the issue using the available Jira workflow and confirm the resulting status. If the agent session ends before merge, leave the issue In Progress and resume this step when merge is observed.

Agents do not merge a PR unless the user explicitly asks. A required Jira credential or unavailable transition must be reported as a blocker to that status update; never claim the issue moved without confirming the result.

## Approval boundary
Agents may inspect, plan, implement on a feature branch and propose a PR. A human must approve consequential changes and merge. An agent must never approve its own PR or bypass required checks.
