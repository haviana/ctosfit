# Testing strategy

Use a risk-based test pyramid:
- **Unit**: fast tests for domain logic, edge cases and error behavior.
- **Component/integration**: verify persistence, messaging, serialization and framework wiring.
- **Contract**: verify compatibility between service boundaries.
- **End-to-end**: a small number of critical user journeys.

Tests should be deterministic, isolated, readable and meaningful. Avoid arbitrary sleeps and dependence on shared mutable environments. Use realistic test data without production personal data. Test authorization boundaries, invalid input, retries, idempotency and failure paths where relevant.

A bug fix should normally include a regression test. Coverage is a signal, not a substitute for test quality. Document any untested risk.
