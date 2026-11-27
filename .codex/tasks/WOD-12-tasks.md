# WOD-12 tasks

- [x] Define route parsing and typed API-client behavior.
  - Acceptance: known role paths resolve deterministically; authenticated calls
    send only the required headers and handle unauthorized responses safely.
  - Verify: `cd web && npm test -- routes api-client`
- [x] Add reusable coach and athlete application shells.
  - Acceptance: both shells expose semantic navigation, role context, and main
    content landmarks.
  - Verify: server-rendering unit tests.
- [x] Integrate route shells with the existing workspace.
  - Acceptance: `/` preserves the landing/demo; `/coach` and `/athlete` select
    their corresponding workspaces without treating the route as authorization.
  - Verify: production build and runtime route inspection.
- [x] Add responsive design-system styles and documentation.
  - Acceptance: 320px and desktop layouts remain usable and documented.
  - Verify: browser inspection plus `git diff --check`.
- [ ] Complete independent review and PR handoff.
  - Acceptance: blockers resolved, PR opened, claim bound to PR, Jira left In
    Progress until merge.
  - Verify: GitHub PR/check evidence and Jira status readback.
