# Product Roadmap

Planning assumption: one founder/developer at approximately 10–15 hours per week. Timing is indicative, not a delivery commitment.

| Phase | Indicative timing | Outcome |
|---|---:|---|
| Discovery | Weeks 1–2 | 10 interviews, workflow mapping, prototype, 3–5 pilot candidates |
| Platform foundation | Weeks 3–4 | Modular backend/frontend, database, auth, tenant isolation, CI |
| Workout Studio | Weeks 5–7 | Exercise library, structured editor, templates, calendar, publication snapshots |
| Athlete workflow | Weeks 8–10 | Mobile workout view, typed result logging, history |
| Performance basics | Weeks 11–12 | Personal records, basic athlete/coach insights |
| Hardening | Weeks 13–14 | Security review, observability, backup/restore, privacy and production readiness |
| Pilot | Weeks 15–16 | Onboard 3–5 boxes, observe usage and iterate |

## Release gates
1. **Problem validation:** interviews show recurring programming/results pain; at least three boxes agree to test a prototype.
2. **Platform readiness:** authentication, authorization and tenant-isolation tests pass before feature rollout.
3. **Coach workflow:** a coach can create and publish a representative workout without assistance; measure time and friction.
4. **Athlete workflow:** athletes can view and log results during real sessions; measure completion and time-to-log.
5. **Pilot readiness:** backup/restore, monitoring, privacy basics and support process are ready.

## Six-month hypotheses
- 3–5 design-partner/pilot boxes.
- 5–10 paying boxes as a commercial hypothesis, not a forecast.
- Measure weekly active coach usage.
- Three-month retention target above 80%, subject to meaningful cohort size.
- Pricing hypotheses: design partners free for 3 months; early access around €29/month; standard around €49/month; future Pro around €79/month. Validate willingness to pay before adopting these prices.

## Budget guardrails
Target approximately €500–€1,000 for initial six-month infrastructure/tooling and keep recurring infrastructure/tooling below €150/month until five active boxes. Excludes founder compensation, legal/accounting, marketing and paid content/licensing.

Prioritize validated pain, activation and retention over feature count. Do not pull billing, booking or wearables into MVP without evidence.