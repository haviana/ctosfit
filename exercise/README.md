# Exercise module

**Owns:** exercise catalog and exercise invariants.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: programming and results must not reach into exercise persistence. Expose a stable application contract when a real cross-module use case is defined. Keep exercise rules out of controllers and consumers.
