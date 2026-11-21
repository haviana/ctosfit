# MVP Requirements

Priority: P0 is required for the first usable pilot; P1 follows validation; P2 is later.

## Identity, box and access — P0
- Users authenticate securely.
- An administrator configures a box and invites members.
- Users have box-scoped roles: owner/admin, coach or athlete.
- Authorization is enforced server-side for every tenant-owned resource.
- Tenant context comes from authenticated membership, never an arbitrary client-supplied tenant identifier.
- Automated tests prove users cannot read or mutate another tenant's data.

## Exercise library — P0
- Coaches create, edit, archive and search exercises.
- Exercise metadata includes stable ID, name, movement category, equipment and optional instructions/media reference.
- Workouts reference exercise IDs.
- Archiving does not corrupt historical publications or results.

## Workout authoring — P0
- A workout contains ordered structured blocks.
- Blocks support type/intent, instructions, movements, targets and optional timing/rest/round metadata.
- Support AMRAP, EMOM, For Time, Tabata, intervals, rounds, sets/reps and chipper.
- Support RX, Scaled and Beginner variants.
- Coaches save drafts, create templates, duplicate workouts and arrange a weekly calendar.
- Validation prevents invalid workouts from being published.

## Publication — P0
- Publishing creates an immutable, versioned snapshot.
- Editing a draft never mutates a previous publication.
- Athletes see the correct publication for the box/date.
- Publication records publisher and timestamp.

## Athlete results — P0
- Mobile-first workout view.
- Athlete selects the applicable prescription.
- Result types: time, load, reps, rounds + reps, distance, calories and completion.
- Result records athlete, tenant, publication, timestamp, prescription and typed score.
- Athlete can review personal workout history.

## Performance
**P0:** identify basic personal bests from valid, comparable scores.

**P1:** benchmark catalogue/history, athlete trends, coach movement-frequency and programming-volume views.

## Non-functional requirements
- Tenant isolation and least-privilege access are mandatory.
- Secrets never enter source control; sensitive operations are auditable.
- Database migrations are versioned.
- Backup and restore are documented and tested before production pilot.
- Structured logs and monitoring must not expose sensitive data.
- Mobile result entry avoids unnecessary fields and typing.
- Domain logic is kept independent of HTTP and persistence frameworks where practical.
- Automated unit, integration, API and tenant-isolation tests.
- REST API documented with OpenAPI.

## MVP acceptance criteria
A box admin invites a coach and athlete; coach creates an exercise, authors, schedules and publishes a structured workout; athlete views it and logs a typed result; athlete sees history; coach sees basic box results; later edits cannot alter published history; cross-tenant read/write tests pass; backup restoration is demonstrated; core workflows work on a modern mobile browser.

## Explicitly excluded
Payments/subscriptions, invoices, booking, waitlists, access control, marketplace, native apps, wearables and AI auto-publishing.