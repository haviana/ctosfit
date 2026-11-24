# Architecture principles

- Start from business capabilities, quality attributes and constraints, not fashionable patterns.
- Define module boundaries and ownership; avoid accidental shared mutable state.
- Depend on abstractions at boundaries when this reduces coupling; do not abstract speculatively.
- Keep domain logic independent of delivery and persistence concerns where practical.
- Make synchronous/asynchronous communication, consistency and failure semantics explicit.
- Define API contracts, versioning and backward-compatibility expectations.
- Treat data ownership, retention, classification and migrations as architectural concerns.
- Record significant decisions as ADRs: context, decision, alternatives, consequences and status.
- Prefer evolutionary architecture and reversible decisions.
- Document key flows, trust boundaries and operational dependencies.
