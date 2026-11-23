# Module boundaries

| Module | Owns | Must not own |
|---|---|---|
| identity | Identity and access concepts | Box, exercise or workout rules |
| box | Gym / training-facility context | Identity provider implementation or workout definitions |
| exercise | Exercise catalog and invariants | Workout programming and athlete results |
| programming | Workout and training-program rules | Exercise persistence or result recording |
| results | Recorded workout outcomes | Workout definition or performance projections |
| performance | Metrics and derived performance views | Raw result persistence or workout programming |

## Dependency policy
1. Business modules are independent Gradle projects.
2. No business module depends on another business module by default.
3. A module never queries another module's tables or repositories.
4. Cross-module collaboration requires an explicit contract and an ADR explaining ownership, direction and necessity.
5. `app` is the composition root and may depend on all business modules.
6. Infrastructure dependencies remain inside the module that owns the adapter.

This conservative policy prevents accidental coupling while the product domain is still being discovered. Relax it with evidence, not convenience.

## Package conventions
A module may use `domain`, `application.port.in`, `application.port.out`, `application.service`, `adapter.in` and `adapter.out`. Do not create empty classes just to make folders visible.
