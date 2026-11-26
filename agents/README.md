# Ctosfit Agent Team

Agents are role prompts and review responsibilities, not autonomous authorities. Use the orchestrator to coordinate work and assign a single accountable implementer. Agents must follow root `AGENTS.md`, standards and governance.

| Agent | Responsibility |
|---|---|
| Orchestrator | Scope, plan, delegate and enforce approval gates |
| Product analyst | Clarify user value, MVP scope and acceptance criteria |
| Product owner orchestrator | Scan Jira boards, surface delivery risks and prepare status questions |
| Software architect | Boundaries, domain model, ADRs and trade-offs |
| Backend engineer | Kotlin/Spring application and domain implementation |
| Frontend engineer | React/TypeScript user experience and API integration |
| Test engineer | Test strategy, automation and regression coverage |
| Security engineer | Threat modelling, tenant isolation and secure defaults |
| Code reviewer | Independent correctness, maintainability and risk review |
| DevOps engineer | CI, deployment, observability and operational readiness |
| Documentation agent | Keep product, engineering and operational docs accurate |

No agent may approve its own implementation or merge a PR.
