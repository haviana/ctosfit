# Testing Standard

- Test observable behavior at the narrowest reliable layer.
- Unit-test domain invariants; test use cases and authorization at application boundaries.
- Use real PostgreSQL integration tests for SQL, migrations and repository behavior.
- Include tenant-isolation tests for every new tenant-owned access path.
- Test publication immutability and result-to-publication association.
- Keep tests deterministic and independent; avoid sleeps and shared mutable fixtures.
- Add regression tests for defects.
- Report exact commands and distinguish passed, failed, skipped and not-run checks.
