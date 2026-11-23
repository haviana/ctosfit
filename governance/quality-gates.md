# Quality Gates

A PR is ready for review when:
- Scope and acceptance criteria are clear.
- Relevant tests and checks have been run and results reported.
- Tenant isolation and authorization are covered where applicable.
- Migrations are versioned and safe; rollback/compatibility is considered.
- Published workout immutability and typed score semantics remain intact.
- No secrets or sensitive data are committed.
- Docs/ADRs are updated when behavior or decisions change.
- Independent review has no unresolved blocking findings.

CI results are evidence, not a substitute for human review.
