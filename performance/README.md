# Performance module

**Owns:** performance metrics, progress and derived performance views.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own performance calculations and projections. Reuse results through explicit application ports; never query another module's tables or repositories. Keep calculations deterministic and unit-testable.
