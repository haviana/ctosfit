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
verifies the already-claimed **In Progress** state on PR opened/reopened and
claim-binding synchronize events and verifies **Done** evidence on merged PR events, but only after verifying the
current PR state, target branch, Jira source status, the authenticated factory
claim branch/record, and a fresh Jira status readback. Configure the repository secrets `JIRA_BASE_URL`,
`JIRA_EMAIL` and `JIRA_API_TOKEN`, plus repository variables
`JIRA_IN_PROGRESS_STATUS_ID` and `JIRA_DONE_STATUS_ID`, before enabling it.
Jira keys are read from the canonical factory head branch; unclaimed branches,
terminal mismatches and ineligible source states are
rejected. This verification workflow does not claim backlog work, check out
code, launch Codex, open PRs or write Jira state. A closed but unmerged PR is a
no-op. The factory performs the Jira **In Progress** transition after claiming
an issue and the **Done** transition only after observing the merged PR; this
single-writer rule avoids replay races between old PRs and new claims.

Backlog claims belong exclusively to an explicit `factory next` invocation.
Configure the repository variable `JIRA_BACKLOG_JQL` with deterministic ordering,
for example `project = WOD AND labels = factory AND statusCategory = "To Do"
ORDER BY Rank ASC`. The factory must verify project `WOD`, the exact `factory`
label and an eligible To Do status, then atomically reserve the persistent
`codex/factory-lock/<ISSUE-KEY>` branch, create the implementation
`codex/factory/<ISSUE-KEY>` branch and create the matching
`.codex/factory-claims/<ISSUE-KEY>.json` record before transitioning Jira.
After opening the PR, the factory updates both claim records with that PR number
before merge. The lock branch is retained as the issue-level serialization
record. Branch-create conflicts, missing claim records and mismatches stop the
run; the factory must not proceed when another writer owns the issue. PR
opened/reopened events only verify the already-claimed In Progress state,
claim-binding commits trigger the same verification, and Done reads the claim
from the immutable merge commit while checking it is still active. After the
factory confirms Jira Done, it records completion on the lock with an expected
blob SHA before a later `factory next` may reuse that issue.

## Safety and approval
AI agents may prepare changes and PRs but cannot approve their own work, merge, bypass checks or perform consequential production actions without explicit human approval. GitHub Actions must use least privilege and must not execute untrusted PR code in privileged contexts.

## Configuration
The AI review workflow is advisory and requires an `OPENAI_API_KEY` repository Actions secret. This key is separate from a ChatGPT subscription. If the secret is absent, the workflow is skipped with a notice; ordinary quality and security workflows remain usable without it.
