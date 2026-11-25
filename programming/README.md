# Programming module

**Owns:** workout and training-program design and delivery.

The initial domain model supports tenant-associated drafts, ordered structured blocks,
typed movement targets, RX/Scaled/Beginner prescriptions, and immutable versioned
publication snapshots. Format details (intervals, rounds, work/rest and time caps) are
stored as typed values. Draft creation, editing and publication resolve a unified trusted
coach context containing the active box and coach identity. Repository lookups require
both the resolved box and workout/publication ID; clients cannot select the tenant scope.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own workout/programming rules. Reference exercise capabilities through an application contract, never its database entities or repositories. Keep workout structure and validation in domain/application layers.

The initial domain model includes editable, versioned `WorkoutDraft`s with ordered
structured blocks and immutable `WorkoutPublication` snapshots. Each draft is tied to
the box ID supplied from resolved trusted tenant context. Movement prescriptions can
provide RX, Scaled, and Beginner variants for athlete selection. Publications capture
the box, title, block content, scheduled date, publisher, and publish timestamp; later
draft edits advance the draft version and cannot change earlier snapshots.

The PostgreSQL adapter stores editable drafts separately from immutable publication
snapshots. Versioned content is encoded by adapter-owned JSON documents; publication
metadata and its movement metric/prescription indexes are stored in relational tables.
`GetPublishedWorkoutSnapshot` is the public application query for consumers such as
results. It requires box scope and returns the exact published snapshot with stable
exercise-based metric IDs and prescription variants available across all movements.

The HTTP adapter exposes:

- `POST /api/coaches/workouts` to create a draft with optional structured blocks.
- `PUT /api/coaches/workouts/{id}/title`, `POST /api/coaches/workouts/{id}/blocks`,
  and block `PUT`, `PATCH .../position`, and `DELETE` routes to edit a draft.
- `POST /api/coaches/workouts/{id}/publish` to publish the current immutable version
  for a date.
- `GET /api/athletes/workouts?date=YYYY-MM-DD` to list that date's publications in
  the currently membership-verified box.
- `GET /api/athletes/publications/{id}` to read a publication in the currently
  membership-verified box context.

Requests describe workout content only; box, coach and athlete identifiers are
resolved by application context and never accepted from request bodies.
