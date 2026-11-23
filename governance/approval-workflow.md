# Approval workflow

## Default autonomy
Agents may inspect, plan, edit working-tree files, add tests and run local non-destructive checks within the user's task.

## Explicit approval required
- Creating, deleting, transferring or changing visibility of repositories.
- Merging pull requests, publishing packages, releasing or deploying.
- Production access, production data changes and database migrations.
- Destructive commands, force pushes, permission changes and secret rotation.
- New paid services, material spend, external communications or irreversible actions.
- Security exceptions, weakened quality gates or major dependency upgrades.

## Review policy
The authoring agent must not be the sole reviewer. Require independent review for application code; security review for authentication, authorization, cryptography, sensitive data and infrastructure. CI success is necessary but not sufficient.

## Decision record
For each approval request, provide the action, target, rationale, impact, risks, rollback plan and exact scope. Wait for explicit user approval before proceeding.
