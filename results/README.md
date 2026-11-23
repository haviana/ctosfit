# Results module

**Owns:** recorded athlete workout results and their lifecycle.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: do not duplicate workout definitions from programming; refer to stable identifiers and contracts. Keep result validation independent of web and persistence frameworks.
