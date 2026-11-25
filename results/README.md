# Results module

**Owns:** recorded athlete workout results and their lifecycle.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: do not duplicate workout definitions from programming; refer to stable identifiers and contracts. Keep result validation independent of web and persistence frameworks.

`WorkoutResult` records tenant, athlete, immutable publication, selected prescription,
submission time and a typed `WorkoutScore`. Score types keep units explicit for loads
and distances. `MetricId` identifies the movement or workout metric that gives a score
comparison meaning; scores from different metrics are not comparable. Personal-best
selection excludes time-cap and did-not-finish results.

The PostgreSQL adapter stores typed score columns with a database check constraint
that permits exactly one valid score shape per result. History queries always bind both
box and athlete IDs and return newest submissions first.

The HTTP adapter exposes `POST /api/athletes/results`, `GET /api/athletes/results`,
`GET /api/athletes/personal-bests`, and `GET /api/coaches/results` for a box-scoped
coach view. Personal bests are grouped by metric, prescription and score semantics;
only completed time attempts are candidates.
Submission bodies contain a publication ID, metric ID, prescription ID and a structured
score object. They do not contain box or athlete IDs; the application resolves those
from the authenticated member context. Submission verifies the publication, metric and
prescription in the selected box before persistence.

Score requests use an explicit `type` and only its matching fields, for example:

```json
{
  "publicationId": "<publication UUID>",
  "metricId": "<movement ID>",
  "prescriptionId": "RX",
  "score": { "type": "ROUNDS_AND_REPS", "rounds": 8, "additionalReps": 3 }
}
```

Supported score types are `LOAD`, `TIME`, `REPS`, `ROUNDS_AND_REPS`, `DISTANCE`,
`CALORIES` and `COMPLETION`. A mismatched field set is rejected at the HTTP boundary.
