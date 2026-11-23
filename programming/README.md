# Programming module

**Owns:** workout and training-program design and delivery.

Hexagonal layout: `domain/`, `application/port/in/`, `application/port/out/`, `application/service/`, `adapter/in/`, `adapter/out/`.

Rules: own workout/programming rules. Reference exercise capabilities through an application contract, never its database entities or repositories. Keep workout structure and validation in domain/application layers.
