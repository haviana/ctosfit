# Box module

**Owns:** gym / training-facility context.

The application layer resolves a selected box into a `TenantContext` only after an
`ActiveMembershipLookup` confirms that the authenticated user has an active membership.
Treat a client-supplied box ID as a selection hint; it is never authorization. The
composition root supplies the membership lookup without creating a business-module
dependency from box to identity.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own the box model and lifecycle; do not expose persistence entities or Spring types; do not depend on other business modules without an ADR. Tenant/box isolation must be designed explicitly, not duplicated ad hoc.
