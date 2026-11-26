# Skill: Jira Workflow Automation

Use this playbook for implementation tasks linked to Jira issues.

## Inputs

Jira issue key, acceptance criteria, repository task, branch or PR context, and
the user's authorization boundaries.

## Procedure

1. Read the issue and inspect its current status and available transitions.
2. Confirm the issue is the intended project and preserve its acceptance
   criteria as the task scope.
3. At the moment implementation begins, the accountable factory/implementer
   transitions to **In Progress** and verifies the returned status. The generic
   PR workflow only verifies this state and never writes Jira.
4. Run the repository's normal implementation, specialist review and
   verification workflow while the issue remains **In Progress**.
5. Create or update the PR as authorized, but do not transition to Done yet.
6. Observe GitHub evidence that the PR was merged into `main` or `master`.
7. After merge, the accountable factory/implementer transitions to **Done**,
   verifies the returned status, and reports the evidence. The generic PR
   workflow only verifies merge and claim evidence and never writes Jira.
8. Do not query or transition the next backlog issue as a side effect of merge.
   Only an explicit `factory next` invocation may select the first eligible
   issue in the configured order, acquire a durable claim/handoff, transition
   it to **In Progress**, and report its key.

## Failure handling

- If the issue key is missing or ambiguous, do not guess or mutate Jira.
- If Jira access or a transition is unavailable, report the blocker and continue
  repository work without claiming the transition happened.
- If the PR is open, approved, or passing checks but not merged, leave Jira In
  Progress.
- If the issue is already Done, verify the state and do not perform a duplicate
  transition.
- If backlog JQL is absent or returns no issue, do not guess the next task. A
  merged PR does not authorize an implicit backlog claim.

## Verification contract

The report must include the issue key, observed before/after statuses, actual
transition result, PR URL, merge evidence, and any unavailable evidence.
