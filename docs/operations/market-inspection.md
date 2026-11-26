# CrossFit Market Inspection

This playbook is the operating artifact for [WOD-44](https://hugohaviana.atlassian.net/browse/WOD-44). It turns market evidence into a small, testable Wodify backlog without treating competitor features or industry forecasts as validated customer demand.

## Owner and cadence

- **Owner:** Product owner / backlog owner.
- **Cadence:** Quarterly, with an ad-hoc update when a material competitor, participation or regulatory change is reported.
- **Output:** One dated decision memo, an updated competitor matrix, and Jira tickets only for opportunities with a validation plan.
- **First run:** The four companion opportunities are [WOD-47](https://hugohaviana.atlassian.net/browse/WOD-47), [WOD-48](https://hugohaviana.atlassian.net/browse/WOD-48), [WOD-46](https://hugohaviana.atlassian.net/browse/WOD-46), and [WOD-45](https://hugohaviana.atlassian.net/browse/WOD-45).

## Run procedure

1. **Collect signals:** Review official CrossFit communications, affiliate/community signals, competitor product pages and reputable fitness-industry research.
2. **Record evidence:** For every signal, record the URL, publisher, publication date, retrieval date, claim, affected persona, confidence and whether it is an observation or a hypothesis.
3. **Scan competitors:** Compare positioning, target users, core workflows, pricing/packaging where public, strengths, gaps and likely switching triggers.
4. **Reconcile with product evidence:** Compare the signal with pilot interviews, usage metrics, support requests and the existing WOD backlog.
5. **Score opportunities:** Use the scorecard below; do not create implementation work from a market signal alone.
6. **Create or update Jira:** A ticket must include evidence links, target persona, a measurable success metric and a pilot/validation plan.
7. **Review outcomes:** At the next run, record what was learned and whether the opportunity was promoted, revised or dropped.

## Competitor matrix

| Competitor / reference | What to inspect | Evidence rule |
|---|---|---|
| Regybox | Positioning, box/coach/athlete workflows, programming and results support, integrations, pricing/packaging, onboarding and retention mechanisms | Use Regybox-owned pages or dated, attributable third-party evidence; label unknowns instead of inferring them. See the [WOD-9 workflow review](regybox-workflow-review.md) |
| Other CrossFit / functional-fitness platforms | Same dimensions as Regybox, plus affiliate/community differentiation | Compare like-for-like workflows and record the date of each observation |
| CrossFit ecosystem | Affiliate model, Open/benchmark participation, education, community and official product direction | Prefer official CrossFit sources; use secondary reporting only as a clearly labelled signal |
| Broader fitness market | Community retention, mobile/wearables, inclusive programming, longevity and event participation | Prefer primary research or named industry reports with methodology |

## Source-quality rules

- **High confidence:** primary source, named publisher, clear publication date and directly observable claim.
- **Medium confidence:** reputable secondary source that cites data or a named primary source, with its limitations recorded.
- **Low confidence:** unattributed commentary, search snippets, social posts without verifiable context or claims that cannot be reproduced.
- Never turn a competitor claim into a Wodify requirement without confirming it with a target user or pilot signal.
- Separate **observation**, **interpretation** and **hypothesis** in the decision memo.

## Opportunity scorecard

Score each dimension from 1 (low) to 5 (high):

| Dimension | Question |
|---|---|
| Customer value | Does this solve a frequent, severe problem for a defined persona? |
| Market momentum | Is the signal persistent and growing, or a one-off headline? |
| Strategic fit | Does it strengthen the workout-first, box-and-athlete product? |
| Evidence strength | Do independent market and pilot signals agree? |
| Effort / risk | Can it be validated safely within the MVP constraints? |

Prioritize opportunities with strong customer value, strategic fit and evidence. A high market score alone is insufficient. Record the score and the reason for every recommendation.

## Research log template

```text
Run date:
Owner:

Signal:
Source URL:
Publisher / publication date / retrieval date:
Persona affected: coach | box owner | athlete | affiliate network
Observation:
Interpretation:
Hypothesis:
Confidence: high | medium | low
Existing WOD issue(s):
Opportunity score: value / momentum / fit / evidence / effort-risk
Decision: create ticket | update ticket | monitor | reject
Validation plan and success metric:
```

## First-run decisions

The first run produced four hypotheses for pilot validation:

- Community retention loops ([WOD-47](https://hugohaviana.atlassian.net/browse/WOD-47)).
- Wearable and recovery-aware insights ([WOD-48](https://hugohaviana.atlassian.net/browse/WOD-48)); this remains a post-MVP hypothesis because wearable integrations are currently out of MVP scope.
- Inclusive onboarding for new and mature athletes ([WOD-46](https://hugohaviana.atlassian.net/browse/WOD-46)).
- Events and benchmarks as an affiliate growth loop ([WOD-45](https://hugohaviana.atlassian.net/browse/WOD-45)).

These are discovery tickets, not a commitment to ship all four. Pilot evidence and the scorecard decide what advances.
