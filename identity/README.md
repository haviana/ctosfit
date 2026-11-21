# Identity module

**Owns:** identity and access concepts.

Hexagonal layout when implementing a use case:
- `domain/`: identity concepts and invariants; framework-free.
- `application/port/in/`: inbound use-case interfaces.
- `application/port/out/`: outbound dependency interfaces.
- `application/service/`: use-case orchestration.
- `adapter/in/`: REST or other inbound adapters.
- `adapter/out/`: persistence and external-provider adapters.

Rules: own the identity model; never expose persistence entities or Spring types through public contracts; do not depend on another business module. Do not invent authentication flows during foundation work.
