# AI-Assisted Development

Ctosfit adopts a curated set of engineering agents, skills, standards and workflows from [AI Development Umbrella](https://github.com/haviana/ai-development-umbrella), adapted to this product.

## How to use
1. Start with the root [AGENTS.md](../../AGENTS.md).
2. Select relevant role definitions in [agents](../../agents/README.md).
3. Follow the task playbook in [skills](../../skills/README.md).
4. Apply [engineering standards](../../standards/engineering-principles.md) and [governance](../../governance/approval-workflow.md).
5. Open a PR with scope, rationale, tests, risks and rollback notes.

## Codex Tasks

Codex tasks use the repository's `AGENTS.md` as durable task guidance. Start a
task from the repository root and provide the requested outcome, acceptance
criteria and constraints in the task prompt. The routing section in
`AGENTS.md` then directs the task through the orchestrator, applicable
specialists, playbooks and independent review. The task result must distinguish
roles that were actually consulted from roles that were not applicable or
unavailable.

For Jira-linked implementation tasks, use `agents/jira-workflow-orchestrator.md`
and `skills/jira-workflow/SKILL.md`. The issue moves to **In Progress** only
when implementation begins and moves to **Done** only after GitHub confirms the
associated PR is merged. Missing credentials, unavailable transitions and
unconfirmed status results remain explicit blockers.

The event-driven implementation is `.github/workflows/jira-workflow.yml`. It
uses PR opened/reopened events for **In Progress** and merged PR events for
**Done**. Configure the repository secrets `JIRA_BASE_URL`, `JIRA_EMAIL` and
`JIRA_API_TOKEN` before enabling it. Jira keys are read from the PR title, body
or head branch, and no transition is attempted when no unambiguous key is
found. After a merged PR, it can claim the next issue returned by the optional
repository variable `JIRA_BACKLOG_JQL` and move that issue to **In Progress**.
The JQL must define the backlog ordering explicitly, for example:
`project = CTF AND statusCategory = "To Do" ORDER BY Rank ASC`. Claiming a Jira
issue does not start arbitrary code execution; a Codex task must use that issue
key as its next task input.

## Safety and approval
AI agents may prepare changes and PRs but cannot approve their own work, merge, bypass checks or perform consequential production actions without explicit human approval. GitHub Actions must use least privilege and must not execute untrusted PR code in privileged contexts.

## Configuration
The AI review workflow is advisory and requires an `OPENAI_API_KEY` repository Actions secret. This key is separate from a ChatGPT subscription. If the secret is absent, the workflow is skipped with a notice; ordinary quality and security workflows remain usable without it.
