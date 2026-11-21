# Adding a use case

1. Identify the owning module and read its README.
2. Model domain concepts and invariants without framework dependencies.
3. Define an inbound port in `application/port/in`.
4. Define outbound ports in `application/port/out` for required external capabilities.
5. Implement orchestration in `application/service`.
6. Add adapters only for actual entry points or external systems.
7. Test domain rules and application orchestration without Spring.
8. Add adapter integration tests where translation or infrastructure behavior needs verification.
9. Update module documentation and add an ADR for significant design choices.

## Reuse checklist
Before extracting shared code, ask:
- Is this behavior genuinely the same business concept?
- Which module owns it?
- Can composition or a focused interface avoid inheritance?
- Does the abstraction have more than one real consumer?

Do not centralize unrelated code simply because it looks similar.
