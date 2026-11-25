# Exercise module

**Owns:** exercise catalog and exercise invariants.

The catalog stores tenant-owned exercise definitions with stable IDs, movement category,
equipment, optional instructions and media reference. Archive preserves the exercise ID
for historical workout references; normal searches hide archived exercises. Use cases
obtain tenant scope from a `CurrentTenantProvider`, while repository ports scope ID
lookups and search by that trusted tenant ID.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

The HTTP adapter exposes coach exercise create, search, update and archive operations.
The JDBC adapter stores exercises and equipment separately, with the verified box ID
included in every repository query and mutation. Authentication and current-box
resolution are supplied by the application composition root through
`CurrentTenantProvider`; request DTOs never set tenant IDs.

Rules: programming and results must not reach into exercise persistence. Expose a stable application contract when a real cross-module use case is defined. Keep exercise rules out of controllers and consumers.
