# Coding standards

- Follow idiomatic conventions and formatter/linter configuration for the selected language.
- Prefer descriptive names, small focused functions and explicit types at public boundaries.
- Avoid hidden side effects, unnecessary global state and premature abstractions.
- Handle errors deliberately; do not swallow exceptions or expose internals to clients.
- Validate external input and encode output for its destination context.
- Keep configuration externalized; fail fast on invalid required configuration.
- Avoid hard-coded secrets, environment-specific values and unexplained constants.
- Comment on intent and non-obvious trade-offs, not line-by-line syntax.
- Remove dead code and unused dependencies.
- Keep public interfaces minimal and document behavior and compatibility.
