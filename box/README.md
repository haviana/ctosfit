# Box module

**Owns:** gym / training-facility context.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own the box model and lifecycle; do not expose persistence entities or Spring types; do not depend on other business modules without an ADR. Tenant/box isolation must be designed explicitly, not duplicated ad hoc.
