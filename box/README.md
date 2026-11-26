# Box module

**Owns:** gym / training-facility context.

The application layer resolves a selected box into a `TenantContext` using the current
user from trusted authentication context and only after an `ActiveMembershipLookup`
confirms active membership. Treat a client-supplied box ID as a selection hint; it is
never authorization. The composition root supplies both outbound ports without creating
a business-module dependency from box to identity.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own the box model and lifecycle; do not expose persistence entities or Spring types; do not depend on other business modules without an ADR. Tenant/box isolation must be designed explicitly, not duplicated ad hoc.

`POST /api/boxes` creates a box and its first owner membership atomically. The owner identity is read from the authenticated principal; it is never accepted in the request body.
