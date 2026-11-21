# Product Vision and Scope

Working name: **Ctosfit**. The repository/product name is provisional and can be changed later without changing the product domain.

## Vision
Build a workout-programming and athlete-performance platform for independent CrossFit and functional-fitness boxes. Help coaches design, publish and improve programming; give athletes a fast way to view workouts, record results and understand progress.

The product is workout-first. It is not initially a full gym-management replacement.

## Initial target customer
Independent boxes with approximately 80–200 active athletes and 2–8 coaches. These figures are hypotheses to validate, not market facts. Prioritize boxes currently using spreadsheets, documents, messaging apps or fragmented tools.

## Users
- **Owner/admin:** configures the box, invites members, manages roles and evaluates adoption.
- **Coach:** maintains exercises, creates workouts and cycles, publishes programming and reviews results.
- **Athlete:** views the published workout on mobile, selects a prescription, records a result and reviews history.

## Product principles
1. Coach remains in control: AI and automation suggest; a coach approves and publishes.
2. Structured data over free text: workouts, blocks, movements and scores are domain data.
3. Mobile-first athlete workflows.
4. Published workouts are immutable historical snapshots.
5. Tenant isolation is a release-blocking requirement.
6. Validate programming before adding full gym-management scope.
7. Boxes should be able to export core workout and result data.

## MVP — P0
- Multi-tenant box model, authentication, roles and membership.
- Exercise library with search and metadata.
- Structured workout/block editor.
- AMRAP, EMOM, For Time, Tabata, intervals, rounds, sets/reps and chipper.
- RX, Scaled and Beginner prescriptions.
- Templates, duplication and weekly calendar.
- Draft/publish workflow with versioned immutable snapshots.
- Athlete PWA/mobile web workout view.
- Typed result logging: time, load, reps, rounds + reps, distance, calories and completion.
- Basic history and personal records.

## Later scope
**P1:** CSV import/export improvements, benchmark management, notifications, expanded insights and coach programming analysis.

**P2:** AI Programming Assistant for multi-week cycle drafts, analysis of prior programming, movement-pattern/volume distribution and proposed adjustments. Coach approval is mandatory. Public API/webhooks only after integration demand is validated.

**Out of MVP:** billing, invoicing, payments, class reservations, waitlists, door access, marketplace, native mobile apps, wearables, microservices, Kafka and Kubernetes.

## Differentiation hypothesis
AI-generated workouts alone are not a defensible differentiator. The stronger hypothesis is an AI Programming Assistant grounded in a box's programming history and athlete outcomes. Explore multi-week cycle planning, movement-pattern distribution, repeated high-volume exposures, comparison of planned work with results and suggested next-week adjustments. Do not claim competitors lack capabilities unless verified; treat non-public details as unknown.

## Initial success measures
- Coach creates and publishes a typical workout in under 5 minutes.
- Athlete logs a typical result in under 20 seconds.
- No cross-tenant data leakage.
- Published history is stable after later edits.
- Core data can be exported.
- Recruit 3–5 pilot boxes and measure weekly coach usage and retention.

## Assumptions to validate
Coach programming pain, athlete logging adoption, value of performance insights, willingness to pay separately for programming software, and whether cycle-level AI adds value beyond generic workout generation.

## Open decisions
Final brand/name, launch geography/languages, exercise media licensing, multi-box athlete membership rules, retention/deletion policy, validated pricing and required integrations.