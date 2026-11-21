# Approval Workflow

## 1. Operating model

Agents may inspect, plan, implement on a branch, and open a pull request within the approved task scope.

The default model is independent review before merge. The implementer must never represent their own review as an independent approval, and must not use a second account or other workaround to simulate independent review.

For a solo-founder project, the absence of another reviewer must not block every low-risk change. A documented solo-founder exception may be used as described below, without changing the meaning of GitHub's approval state.

## 2. Risk classification and merge policy

Classify each pull request by the highest-risk change it contains.

### Low risk

Examples:
- Documentation, spelling, formatting, and comments.
- Non-functional refactoring with no change to behavior or public contracts.
- Test-only changes that do not weaken or remove security or quality gates.

Policy:
- The author may self-merge after all required CI checks pass.
- Review the complete diff and confirm that no generated files, secrets, or unrelated changes are included.
- An AI review is useful but optional for straightforward changes.

### Medium risk

Examples:
- Ordinary features and bug fixes within an established bounded context.
- Internal implementation changes that preserve API contracts, authorization rules, tenant boundaries, and data semantics.
- Dependency updates that do not materially change runtime, security, or deployment behavior.

Policy:
- The author may self-merge when required CI checks pass, an AI code review has been run where configured, and all actionable findings are resolved or explicitly documented.
- The pull request must include a concise author self-review covering behavior, tests, compatibility, and operational impact.
- If the change has unclear or unexpectedly broad impact, treat it as high risk.

### High risk — independent human approval required

Examples:
- Authentication, authorization, identity, or session management.
- Tenant isolation, tenant resolution, or cross-tenant data access.
- Security controls, secrets, permissions, CI workflow privileges, release/deployment controls, or supply-chain protections.
- Database migrations, destructive or irreversible data changes, data retention, or sensitive-data handling.
- Payment, billing, financial integrity, or other correctness-critical workflows.
- Public API or event-contract changes with material compatibility impact.
- Production infrastructure changes, new external integrations, or material architecture changes.
- Changes to this approval policy or other governance controls.

Policy:
- An authorized human other than the implementer must review and approve before merge.
- AI review and passing CI are additional safeguards; neither substitutes for independent human approval.
- If no independent reviewer is available, defer the merge. Do not lower the risk classification merely to unblock delivery.

## 3. Solo-founder self-merge procedure

For low- and medium-risk pull requests where independent review is not reasonably available, the author may use the solo-founder exception:

1. State in the pull request that the solo-founder exception is being used and identify the risk level.
2. Read the complete diff, not only the summary.
3. Confirm that relevant tests and required CI checks pass.
4. Run the configured AI review for medium-risk changes and resolve or document its findings.
5. Record a short self-review in the pull request: intended behavior, tests performed, risks considered, and why independent review is not required for this change.
6. Merge only when repository rules permit it and no required check is bypassed.

This procedure is a documented self-review and merge exception. It is not a GitHub approval and must never be reported as an independent approval.

## 4. Non-negotiable controls

- Never bypass required CI checks or branch protection to merge a change.
- Never approve your own pull request or use another account to manufacture an approval.
- Never treat an AI-generated review as a human approval.
- High-risk changes always require independent human approval, even in solo-founder mode.
- If a pull request mixes low/medium-risk work with high-risk work, the high-risk policy applies to the entire pull request. Split the changes when practical.
- Changes to approval rules, security gates, workflow permissions, or branch-protection configuration require independent review before they take effect.

## 5. Pull request self-review checklist

For every solo-founder self-merge, record answers to the following:

- [ ] The change is correctly classified as low or medium risk.
- [ ] I reviewed the complete diff and checked for unrelated changes or exposed secrets.
- [ ] Relevant tests were added or updated, and required CI checks passed.
- [ ] Compatibility, error handling, and operational impact were considered.
- [ ] AI review findings were addressed or explicitly documented when AI review is required.
- [ ] No required check or repository protection was bypassed.
- [ ] The pull request explains why independent human approval is not required under this exception.
