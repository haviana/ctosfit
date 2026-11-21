# ADR 0002: AI-Assisted Development Governance

- Status: Accepted
- Date: 2026-11-21

## Context
Ctosfit benefits from reusable AI agent roles, task skills, engineering standards and automated review support. These tools must not weaken security, quality or human accountability.

## Decision
Adopt a curated local snapshot of the AI Development Umbrella, adapted to this product. Agents may plan and implement scoped changes on branches and open pull requests. Consequential changes and merges require human approval. An implementer cannot approve their own work.

Automated AI review is advisory. It reads pull request metadata and patches through the GitHub API and must not check out or execute untrusted pull-request code in a privileged context. Standard CI and security scans remain independent of the OpenAI API key.

## Consequences
- Role prompts and task playbooks are versioned in this repository.
- Project-specific invariants remain authoritative in root `AGENTS.md`.
- Upstream updates are reviewed and adapted through pull requests.
- AI output is not treated as proof that tests passed or that a change is secure.
