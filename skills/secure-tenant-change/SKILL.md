# Skill: Secure Tenant-Scoped Change

1. Identify all tenant-owned entities and their owning module.
2. Trace identity to authenticated user, membership, role and trusted tenant context.
3. Ensure tenant context is resolved server-side for every use case.
4. Scope repository reads, writes, updates, deletes, counts and nested lookups.
5. Verify unique constraints include tenant scope where required.
6. Test cross-tenant access, guessed IDs, nested resources, list/search endpoints and role escalation.
7. Review logs, errors, caches, exports and background jobs for tenant leakage.
8. Document residual risks and request security review for sensitive changes.

A client-provided box/tenant identifier may select context only after membership authorization; it is never proof of access.
