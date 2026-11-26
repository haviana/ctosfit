# WOD-9 Mini-Spec

## Objective

Produce a dated competitor workflow review for RegyBox 2.0 that covers workout
editor, AI programming, athlete logging, analytics, integrations and exports.
Separate public/vendor-documented claims from capabilities verified in a live
demo, and translate only evidence-backed observations into Ctosfit validation
hypotheses.

## Acceptance criteria

- One discoverable Markdown artifact contains the scope, date, review method,
  comparison matrix, source register, findings and follow-up checklist.
- All six requested areas have a distinct persona, repeatable verification step,
  Ctosfit baseline, verdict and unresolved next check.
- Public sources are linked with retrieval date, confidence and limitations.
- No marketing claim is labelled as live-demo verification.
- Completion requires a dated live-demo record for all six areas, including role,
  action, visible outcome, evidence ID and remaining unknowns. Until that exists,
  the Jira issue remains In Progress even if the desk-research PR is merged.
- Existing market-inspection and discovery docs link to the artifact.
- No product code, API contract, architecture boundary or MVP scope is changed.

## Constraints and assumptions

- No authenticated RegyBox demo account or vendor session is available in this
  run; live-demo acceptance remains open.
- Public vendor pages and the app-store listing are evidence of documented
  claims only.
- Ctosfit remains workout-first; competitor breadth is context, not a reason to
  add gym-management scope.
- No credentials, real athlete data or restricted demo material may enter the
  repository.

## Verification

- `git diff --check`
- Markdown structure and link review.
- Independent documentation/product review before PR readiness.
