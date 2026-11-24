# ADR 0004: Resolve tenant context through verified membership

- Status: Accepted
- Date: 2026-11-24

## Context

Every tenant-owned operation needs a trusted box scope. A box ID supplied by a caller
can select the requested context, but cannot prove that the caller belongs to that box.
The identity and box business modules remain independently owned and must not depend
on each other's repositories or implementation details.

## Decision

The box module exposes a use case that takes the selected box ID, obtains the current
user through an `AuthenticatedUserProvider` backed by trusted authentication context,
asks an `ActiveMembershipLookup` port to verify active membership, then returns a
`TenantContext`. The tenant context can only be constructed inside the box module's
authorization flow. The application composition root is responsible for connecting
both ports to authentication and identity's membership capability.

All tenant-owned use cases and repository operations must receive this resolved context
and scope their reads and writes to its box ID. New modules must not accept raw request
box IDs as authorization context.

## Consequences

- A request-selected box is rejected unless active membership is verified.
- The box module has no dependency on the identity module.
- The current repository has no authentication adapter, membership store, or
  tenant-owned persistence yet. Those integrations and PostgreSQL cross-tenant tests
  must be added with their owning implementation stories.
