# Coding Standard

- Follow established formatting, naming and language idioms in the repository.
- Prefer small cohesive functions and explicit names.
- Avoid boolean-parameter APIs, hidden side effects and broad catch-all exception handling.
- Validate untrusted input at boundaries; enforce invariants in domain/application layers.
- Do not leak persistence entities or framework types into public domain contracts.
- Avoid unnecessary interfaces, inheritance, generic frameworks and one-line wrappers.
- Keep configuration externalized and secrets out of source control.
- Make errors actionable without exposing sensitive internals.
