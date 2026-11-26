# RegyBox 2.0 Workflow Review

Status: partial review — public evidence collected; live-demo verification remains open.

Review date: 2026-11-26 (Europe/Lisbon)

Jira: [WOD-9](https://hugohaviana.atlassian.net/browse/WOD-9)

## Scope and method

This review covers the six areas requested by WOD-9: workout editor, AI
programming, athlete logging, analytics, integrations and exports. It separates
vendor-documented claims from capabilities actually verified in a live session.

No authenticated RegyBox demonstration account or vendor session was available
for this run. Therefore, every public-page observation below is marked
**Documented claim**, not **Verified in live demo**. No credentials, real athlete
data or restricted demo material were used.

Personas used for follow-up: coach, athlete and box owner/admin. The Ctosfit
baseline comes from the workout-first MVP, discovery scenarios and market
inspection rules in the repository.

## Comparison matrix

| Area / persona | Capability and verification step | Publicly documented observation | Demo verdict | Ctosfit baseline | Unknown / next check |
|---|---|---|---|---|---|
| Workout editor / coach | Build a two-block workout with RX and Scaled variants, schedule and publish it, then edit the draft and reopen the athlete session | RegyBox documents planning by day, week and group, a movement/video library, timers, WOD creation/editing and programming shared across locations. [E01][E02][E03] | Documented claim; live behavior unknown | P0 structured authoring, scheduling, draft/publication separation and immutable published snapshots | Use a coach demo account. Verify variants, draft edits, publication history and whether an athlete’s prior session remains tied to the exact published version |
| AI programming / coach | Request a week draft using previous programming, revise a suggestion and identify who can publish | RegyBox documents AI-assisted plan generation from criteria and AI-assisted planning shared across boxes. Generation is documented; history use, editing and approval are separate checks. [E01][E02] | Documented claims; each live capability unknown | P2 assistance only; coach approval is required before publication | Verify inputs, history use, generated output, editability, audit trail and approval gate. Do not infer history-aware generation from “AI-assisted” marketing copy |
| Athlete logging / athlete | Open a published workout on a phone, select Scaled, log rounds plus reps and reopen personal history | RegyBox documents member score logging, personal records, benchmark history, leaderboards and workout history/downloads. [E01][E02][E03] | Documented claim; live behavior unknown | P0 mobile-first typed scores, units, prescription/variant and history | Verify score-type coverage, units, validation, offline/slow-network behavior, edit rules and exact published-workout reference |
| Analytics / athlete + coach | Compare two results for one prescription; inspect box results and movement frequency across a cycle | RegyBox documents personal records, benchmark rankings, dashboards, period comparisons and app-usage monitoring. [E01][E02][E03] | Documented claim; live behavior unknown | P0 comparable personal progress; P1 coach trends and programming analysis | Verify metric definitions, filters, tenant/role visibility, exportable source data and whether movement-frequency analysis exists |
| Integrations / box owner/admin | Demonstrate one named connection using sample data; identify source, destination, direction and received record | Public materials mention website/social lead capture, payment methods, access control, scale/API readings and hosted video sources, but do not establish a bounded public integration contract for the requested workflow. [E01][E04] | Unknown | Public APIs/webhooks only after validated demand; wearables are outside MVP | Request one integration walkthrough and API/webhook documentation. Record auth, direction, retries, ownership, data minimization and failure handling |
| Exports / box owner/admin | Export one workout and result; inspect whether workout reference, prescription, score and unit survive | RegyBox documents PDF/Excel reports and exportable listings, plus member history/downloads. [E01][E03] | Documented claim; live behavior unknown | Core data portability is a pilot expectation; richer CSV import/export is P1 | Verify role access, formats, fields, identifiers, units, version references and whether an export can reconstruct the athlete result |

### Interpretation rules

- A public feature page or app-store description supports a **Documented claim**
  only. It does not prove the workflow, role permissions, data model or current
  plan availability.
- A capability is **Verified in live demo** only after the reviewer performs
  the step and records the visible result, product version/plan, role and date.
- Keep observation, interpretation and hypothesis separate. Do not convert a
  competitor claim into a Ctosfit requirement without a target-user or pilot
  signal.

## Findings and follow-up

### Observations

1. RegyBox positions workouts and results as one part of a broad all-in-one box
   platform. Its public feature page lists workout planning, AI-assisted plans,
   score logging, personal records, rankings and timers. [E01]
2. Its cross-training page emphasizes a daily WOD, score logging, leaderboards,
   personal records, benchmark history and AI-assisted planning. [E02]
3. The public app listing describes coach WOD editing and score logging on behalf
   of members, athlete score logging, workout history, rankings and competition
   workflows. [E03]
4. Public pricing is not listed as a fixed amount. The FAQ says pricing depends
   on business size/type and is shown during a demonstration. [E04]

### Interpretations

- The strongest public overlap with Ctosfit’s MVP is the coach-to-athlete path:
  plan a WOD, publish it, log a score and review progress. Score types, units
  and validation remain live-demo questions.
- RegyBox’s broader management surface is useful competitive context, but it is
  not evidence that Ctosfit should expand beyond workout-first scope.
- AI assistance should remain subordinate to coach review, and any future
  comparison must inspect approval and audit behavior rather than only generation.

### Hypotheses to validate

| Hypothesis | Persona | Pilot question | Success signal | Existing scope |
|---|---|---|---|---|
| Fast typed result entry is a switching factor | Athlete | Can an athlete log a result with the correct variant, score type and unit without coach help? | Median time-to-log and correction rate | MVP |
| Immutable publication history is a trust differentiator | Coach + athlete | Can a coach correct a draft without changing what an athlete previously completed? | Zero historical-result ambiguity in pilot scenarios | MVP invariant |
| AI assistance matters only with controlled coach review | Coach | Does a reviewed suggestion reduce programming time without creating unsafe/unapproved publication? | Coach time saved with 100% approval before publish | Post-MVP hypothesis |
| Export completeness affects pilot confidence | Box owner | Can the owner reconstruct a workout/result outside the product? | Export contains workout reference, prescription, score, unit and version | Pilot expectation |

## Live-demo checklist

- [ ] Record session date/time zone, product version/build, plan and platform.
- [ ] Record roles available and whether the reviewer operated or observed the demo.
- [ ] Run the six workflow tasks in the comparison matrix.
- [ ] For each row, record visible action/outcome and stable evidence ID.
- [ ] Record access limitations and unresolved follow-ups.
- [ ] Re-score opportunities using the market-inspection scorecard only after
      pilot evidence is available.

## Source register

| ID | Type | Source | Publication/update | Retrieved | Confidence | Limitation |
|---|---|---|---|---|---|---|
| E01 | Vendor feature page | [RegyBox Features](https://www.regybox.com/php8/website/funcionalidades) | Not disclosed | 2026-11-26 | Medium for documented claims | Marketing page; no live role or plan verification |
| E02 | Vendor solution page | [RegyBox Cross training](https://www.regybox.com/php8/website/modalidades/cross-training) | Not disclosed | 2026-11-26 | Medium for documented claims | Summary page; workflows and permissions not demonstrated |
| E03 | App-store listing | [RegyBox on Google Play](https://play.google.com/store/apps/details?hl=en_GB&id=com.zalox.regybox) | Updated 2026-09-02 | 2026-11-26 | Medium for documented claims | Developer-provided listing; app behavior was not tested |
| E04 | Vendor FAQ | [RegyBox FAQ](https://www.regybox.com/php8/website/perguntas-frequentes) | Not disclosed | 2026-11-26 | Medium for pricing/onboarding claims | Pricing remains quote-based; no contract or plan comparison |

## Repository references

- [Market inspection playbook](market-inspection.md)
- [Discovery and pilot plan](discovery-and-pilot.md)
- [Product vision and scope](../product/vision-and-scope.md)
- [MVP requirements](../product/mvp-requirements.md)
