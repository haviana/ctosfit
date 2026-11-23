# Quality gates

A pull request should satisfy the applicable gates:
1. Scope and acceptance criteria are clear.
2. Formatting, linting and static analysis pass.
3. Relevant unit and integration tests pass.
4. Dependency and secret scans complete without unresolved critical findings.
5. API/schema compatibility and migration impact are assessed.
6. Independent code review is complete.
7. Documentation and operational guidance are updated.
8. CI evidence is linked and reproducible.

Projects may add stricter gates. Any exception must be documented, time-bounded, risk-assessed and approved by a human maintainer. Never silently skip a failing gate.
