# Delivery evidence contract

The final delivery record must contain:

1. **Goal and non-goals** — the Jira key, acceptance criteria, assumptions and
   explicit scope boundary.
2. **Repository context** — relevant modules, product/architecture decisions,
   standards and governance files inspected.
3. **Routing** — every selected role, why it was selected, its actual output,
   and any unavailable or not-applicable role.
4. **Specification traceability** — spec/plan/task paths, approval status
   (`required`, `pending`, `approved` or `not required`), approved scope,
   spec revision identifier, task-to-acceptance mapping, scope-change
   reapproval evidence, and the authorized human approver. AI review or
   self-review never satisfies the human approval gate.
5. **Parallel lane** — lane ID, Jira key, worktree path, branch, base commit,
   implementer/model context and planned ownership conflicts, when parallel
   mode is used.
6. **Implementation** — one accountable implementer, changed files/modules,
   migrations, API or compatibility impact, and ADRs when needed.
7. **Security** — trust boundaries, authorization/tenant checks, secret and
   dependency handling, findings, regression tests and residual risk.
8. **Verification** — exact commands, observed exit/status results, CI evidence,
   and checks not run or not applicable.
9. **Independent review** — reviewer identity/role, actionable findings,
   resolutions and remaining suggestions. AI review is not human approval.
10. **Jira lifecycle** — status before and after each attempted transition,
   transition name, confirmed result, durable claim lock/handoff evidence and
   any blocker.
11. **PR/release** — PR URL, target branch, checks, merge evidence, deployment
   or rollback evidence when applicable. For completion, include repository,
   PR URL, `mergedAt` and `baseRefName`; also record the human approval still
   required.

Use observations and recommendations distinctly. If evidence is missing, say so
instead of inferring success. Redact secrets, tokens, authorization headers,
personal data and sensitive infrastructure details unconditionally. Preserve
command structure with variable names or placeholders, record exit/status
results, and include only sanitized output needed to verify the result.

## Copyable report shape

```markdown
# Jira delivery: ISSUE-123

## Goal and scope
- Goal:
- Non-goals:
- Acceptance criteria:
- Spec/plan/tasks:
- Spec approval status and authorized human approver:
- Spec revision approved:
- Task-to-acceptance mapping and scope-change reapproval:
- Sanitized claim/handoff reference:
- Parallel lane/worktree/branch/base commit (if applicable):
- Runtime allocation (ports, URLs, database/Compose instance):

## Routing ledger
| Trigger | Role/playbook | Model/context | Evidence | Findings | Disposition |
|---|---|---|---|---|---|

## Implementation
- Implementer:
- Files/modules:
- API/data/migration impact:
- ADRs:

## Verification
| Criterion/task ID | Command or check | Result | Evidence/limitation |
|---|---|---|---|

## Jira and PR lifecycle
- Jira status before/after each transition:
- Transition names and confirmed results:
- PR URL and target branch:
- Checks and merge evidence:
- Deployment/rollback evidence:

## Security and residual risk
- Findings and fixes:
- Unavailable/not-applicable checks:
- Human approval still required:
```
