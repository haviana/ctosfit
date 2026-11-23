---
description: Hexagonal architecture and module dependency rules
paths:
  - "**/*.kt"
  - "**/*.kts"
---

# Architecture rules

- Keep domain and application layers framework-independent.
- Put inbound ports in `application/port/in` and outbound ports in `application/port/out`.
- Put adapters in `adapter/in` and `adapter/out`.
- Dependencies point inward; adapters implement ports.
- Business modules do not access each other's persistence or internal classes.
- Keep the app module as the only composition root.
- Prefer interfaces and composition. Use abstract classes only for a real shared algorithm or invariant.
- Do not duplicate behavior; give shared behavior one clear owner.
