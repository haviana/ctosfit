# WOD-12 implementation plan

1. Define and test the route and API-client contracts.
2. Add a shared role-aware application shell and server-rendering tests.
3. Wire the existing authenticated workspace to `/coach` and `/athlete` while
   preserving the landing page and demo.
4. Add responsive shell styling and concise web-app documentation.
5. Run tests, production build, route runtime checks, and independent review.

Risks: client routes must not become authorization checks; the box selector is
only context selection; direct-route deployment depends on the existing Render
rewrite; current agent-credit limits may delay independent specialist review.

Human approval point: the PR remains unmerged and Jira remains In Progress until
an authorized human merges it.
