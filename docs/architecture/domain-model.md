# Domain Model — Initial Proposal

This is a starting model to validate with coaches and athletes, not a final database schema.

## Core concepts
- **Tenant/Box:** customer organization; tenant-owned data is isolated by tenant_id.
- **User and Membership:** a person authenticates as a user; box-scoped membership grants owner/admin, coach or athlete access. Do not assume a user belongs to only one box.
- **Exercise:** reusable movement definition with stable identity and metadata; archiving must preserve historical references.
- **Workout Template:** reusable starting structure, not a scheduled/published workout.
- **Workout Draft:** editable workout authored by a coach, composed of ordered structured blocks.
- **Workout Publication:** immutable snapshot of a draft/version, associated with a box and scheduled date/time.
- **Workout Result:** athlete submission against a publication, including prescription and typed score.
- **Benchmark:** defined test/workout used for comparison; version definitions when changes affect comparability.
- **Personal Record:** derived best result for an athlete and comparable metric/benchmark; source results remain authoritative.

## Workout structure
A workout contains ordered blocks. Each block has a type/intent (warm-up, strength, conditioning, skill, accessory), instructions, one or more movement prescriptions, optional duration/interval/rest/round/set/repetition structure and optional RX/Scaled/Beginner variants.

Represent AMRAP, EMOM, For Time, Tabata, intervals, rounds, sets/reps and chipper as structured data. Do not reduce a workout to a single text field.

## Typed scores
- **Time:** duration, precision and completion semantics.
- **Load:** numeric value and unit (e.g. kg).
- **Reps:** integer count.
- **Rounds + reps:** completed rounds plus additional reps.
- **Distance:** numeric value and unit.
- **Calories:** numeric count.
- **Completion:** completed/not completed, optionally with context.
- **Composite:** explicit multiple scored components where one scalar is insufficient.

Comparison is score-type-specific. Lower time may be better for a completed For Time workout; higher load may be better for a lift. Time caps, DNF and tie-breaking need explicit semantics. Never compare incompatible units or score types.

## Historical integrity
- Draft changes do not mutate a publication.
- Publication stores a complete snapshot of workout structure and variants.
- Result points to a publication and records score semantics at submission time.
- Exercise rename/archive does not unexpectedly rewrite historical display data.
- Benchmark changes are versioned when they affect interpretation.

## Candidate relationships
Tenant has memberships, exercises, templates, drafts and publications. User has memberships. Draft has ordered blocks. Publication is created from a draft/version and stores a full snapshot. Athlete membership has results. Result belongs to one athlete and one publication. Performance records derive from valid results and benchmark definitions.

## Decisions to validate
Can a publication be shared across boxes? Can athletes submit multiple scores for one publication? How are partner/team workouts represented? How are time caps, no-score and DNF represented? How are units normalized? Are custom exercises tenant-specific? Can submitted results be edited/deleted? Which benchmarks are globally curated and versioned?