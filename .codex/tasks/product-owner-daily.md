# Daily Product Owner Report

Use `agents/product-owner-orchestrator.md` as the operating procedure.

## Schedule and scope

- Run daily.
- Scan every Jira board and project accessible to the connected account.
- Use the active sprint as the primary scope.
- If no active sprint exists, use open issues updated in the last fourteen days,
  plus overdue or blocked issues.
- Use the last seven days as the activity window when an active sprint exists.

## Required behavior

This task is strictly read-only. Do not transition, assign, edit, comment on,
close, delete or create Jira issues. Do not send messages or contact owners.
Prepare questions for follow-up, but leave outreach for explicit approval.

Collect issue keys, direct links, board/project, summary, status, assignee,
priority, sprint, updated time, due date, blockers/dependencies, comments and
linked pull requests where available. Distinguish inaccessible boards and
missing data from empty results.

## Report

Return:

1. Scan timestamp and reporting window.
2. Boards/projects scanned and coverage gaps.
3. Aggregate counts by board and normalized status.
4. Completed, active, blocked, stale, overdue and unassigned highlights.
5. One concise status-question set per owner or issue:
   - What changed since the last update?
   - What is the next concrete deliverable and expected date?
   - What is blocked or dependent on another person/team?
   - Is the current status and target date still accurate?
6. Decisions or escalations needed from the Product Owner.
7. Verification limits and data that could not be observed.

Every factual statement must include an issue key, board/project context, or a
clear indication that it is an aggregate derived from the scan.
