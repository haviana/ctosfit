# Skill: Feature Delivery

## Inputs
User outcome, target role (coach/athlete/admin), acceptance criteria, constraints and relevant product docs.

## Procedure
1. Inspect current implementation, module boundaries, API contract, ADRs and tests.
2. Confirm MVP fit; record assumptions and non-goals.
3. Define domain behavior and acceptance criteria before choosing implementation details.
4. Place behavior in the owning module; keep adapters thin and domain framework-independent.
5. Define API and persistence changes, including migration and compatibility impact.
6. Implement the smallest complete vertical slice.
7. Add tests at domain, application, integration and API levels as appropriate.
8. Update docs and provide exact verification results.

## Done when
Acceptance criteria are met; authorization and tenant scope are tested; migrations are safe; relevant checks pass; docs are updated; an independent review is requested.
