# Engineering Principles

- Product value and correctness before abstraction or infrastructure.
- Modular monolith first; explicit module ownership.
- Hexagonal architecture: domain/application inward, adapters outward.
- Framework-independent domain logic where practical.
- Interfaces at meaningful boundaries, not by default.
- No duplicated business rules; shared code must have clear ownership.
- Secure by default, especially tenant isolation and authorization.
- Observable, testable and operable changes.
- Prefer incremental, reversible delivery and documented decisions.
