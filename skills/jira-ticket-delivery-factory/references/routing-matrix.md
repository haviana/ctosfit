# Routing matrix

| Trigger in the ticket or affected diff | Required specialist | Minimum responsibility |
|---|---|---|
| Any Jira-linked implementation | Orchestrator; Jira workflow orchestrator; one implementer | Scope, ownership, Jira lifecycle and accountable delivery |
| Behavior or acceptance-criteria change | Product analyst; test engineer | MVP fit, observable acceptance and risk-based test plan |
| Module boundary, API contract, persistence, framework or architecture decision | Software architect | Ownership, coupling, compatibility, migration and ADR need |
| Kotlin/Spring, domain, persistence or REST backend | Backend engineer | Domain/application/adapters and authorization-aware implementation |
| React/TypeScript, UI or API client | Frontend engineer | Accessible states, typed contracts and API-authoritative permissions |
| Authentication, authorization, tenant scope, secrets, sensitive data, dependencies or CI permissions | Security engineer; secure-change playbook; secure-tenant playbook when tenant data is involved | Threat assessment, least privilege, isolation checks and security regression tests |
| Build, CI/CD, Render, runtime configuration, release or operations | DevOps engineer | Repeatable delivery, least privilege, health checks, rollback and observability |
| User-facing setup, API, architecture, configuration or operational guidance | Documentation agent | Accurate task-oriented docs and migration/rollback guidance |
| Measured bottleneck, explicit latency/throughput goal or material performance risk | Performance engineer | Baseline, workload, evidence and representative benchmark |
| Every non-trivial implementation, after edits | Independent code reviewer | Full-diff review; correctness, security, data, tests and architecture findings |

## Ordering

1. Run product, architecture, security, test, DevOps and documentation analysis
   as applicable before implementation.
2. Choose exactly one implementer. Specialists may advise but must not duplicate
   the implementation.
3. Run focused tests and security checks during implementation, then broader
   verification as justified.
4. Run the independent code review after the final diff and resolve or document
   findings before reporting readiness.

Do not invoke performance review for routine work without a measured trigger.
Do not claim a specialist ran when the role was unavailable or only inferred.
