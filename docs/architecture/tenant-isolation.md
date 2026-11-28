# Tenant isolation

Ctosfit stores every tenant in one PostgreSQL database. A row is tenant-owned when it carries `box_id`. The box id on the request is a context selector. It becomes authorization only after the server proves the authenticated user has an **active** membership in that box.

## Request path

1. The API is stateless. A bearer JWT identifies the user (`sub`) and may carry recognized role claims: `BOX_OWNER`, `COACH`, `ATHLETE`. Unknown role claims are ignored.
2. Spring Security uses those claims only to choose a route group. They do not prove box membership.
3. Tenant-owned use cases read `X-Box-Id`. The header must be present and a UUID. The value selects the box; it is not trusted on its own.
4. `ResolveTenantContext` loads the caller's membership for that box and keeps it only when the status is `ACTIVE`. No row, or an `INACTIVE` row, is forbidden.
5. The use case then requires the membership role that operation needs. A coach token cannot manage memberships. An athlete membership cannot call coach use cases, even when the token claim says `COACH`.
6. Repositories receive that resolved box id and bind it on every read and write. Clients do not send a box id in the body, and guessed resource ids are looked up with the resolved box.

| Route group | Token claim required | Active membership role required |
| --- | --- | --- |
| `POST /api/boxes` | authenticated | none; the caller becomes owner of the new box |
| `/api/box-owner/memberships` and `.../{userId}` | authenticated | `BOX_OWNER` in the selected box |
| other `/api/box-owner/**` | `BOX_OWNER` | `BOX_OWNER` in the selected box |
| `/api/coaches/**` | `BOX_OWNER` or `COACH` | `BOX_OWNER` or `COACH` in the selected box |
| `/api/athletes/**` | `ATHLETE` | `ATHLETE` in the selected box |
| any other `/api/**` | denied | denied |

Missing or non-UUID `X-Box-Id` is `400`. A box the caller cannot access, including a guessed box id or an inactive membership, is `403`. A resource id that does not exist **in the selected box** is `404`, including when that id belongs to another box the caller can also access.

## What is scoped

| Resource | Owned by | Cross-tenant behavior under test |
| --- | --- | --- |
| Exercises | exercise module | Search, update, and archive use the selected box. Another box's id is not found. Archive does not hide a row from a foreign search. |
| Workout drafts and nested blocks | programming module | Rename, add, replace, move, delete block, and publish resolve the draft by box and id. A foreign or guessed id does not change the draft. |
| Publications | programming module | Schedule lists and publication reads are limited to the selected box. A member of both boxes still cannot read the other box's snapshot by id. Publishing a draft whose movement id exists only in another box is rejected and stores no snapshot. |
| Results and personal bests | results module | Submit checks the publication, metric, and prescription in the selected box. History and personal bests are box plus athlete. Coach results are the selected box only. One athlete cannot read another's result ids. |
| Memberships | identity module | List, add, role change, and deactivate run as the selected box's active owner. A user who exists only in another box is not found. The caller's own owner role cannot be removed. |

Published snapshots stay immutable. Later draft edits do not change a publication, and a result references that publication id.

PostgreSQL foreign keys repeat the same box id (`exercises`, `workout_drafts`, `workout_publications`, `publication_metrics`, `publication_prescriptions`, `workout_results`, `box_memberships`). They are a backstop. They do not replace the membership check. Row-level security is not enabled.

## Tests

Two Testcontainers suites talk to PostgreSQL 17 and the Flyway schema. Docker is required; the suites are not disabled when Docker is absent, so a missing daemon fails the build.

- `ExerciseTenantIsolationIntegrationTest` covers box onboarding, owner-only membership changes, the last-owner rule, exercise list and update isolation, and the coach-publish / athlete-result path.
- `PostgreSqlTenantIsolationTest` is the cross-tenant denial suite. For reads, writes, lists, nested blocks, and guessed ids it uses a caller who is an **active member of both boxes**, so success in the caller's other box cannot be mistaken for a missing membership. Separate cases cover owner, coach, and athlete boundaries, token claims that disagree with the membership role, inactive memberships, and access that remains in a second box where the same user is still active.

Both classes carry the JUnit tag `tenant-isolation`.

```bash
./gradlew :app:test -PincludeTags=tenant-isolation
```

`./gradlew test` and `./gradlew check` run the suites as part of the full test task. CI checks that Docker is available, runs the build, and fails unless the JUnit reports match exactly: 26 tests in `PostgreSqlTenantIsolationTest` and 5 tests in `ExerciseTenantIsolationIntegrationTest`, with no failures, errors, or skips. Adding or removing a case requires the same change in `scripts/require-tenant-isolation-results.sh`. See [quality and CI](../development/quality-and-ci.md).

## Residual risk

- Invitation accept and delivery are outside this suite.
- The database connection is not restricted with row-level security. A query that forgets `box_id` would not be stopped by PostgreSQL.
- Backup restore is still a separate MVP check.
