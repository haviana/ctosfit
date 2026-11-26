---
name: jira-status-monitor
description: Scan accessible Jira boards and prepare evidence-based delivery status questions for the product owner.
---

# Jira Status Monitor

Use this playbook when the user asks for a Jira-wide delivery check-in, board
monitoring, stale-work detection or current task status questions.

## Read-only default

The scan is read-only. Do not transition issues, add comments, assign work or
send messages unless the user explicitly asks for that follow-up after reviewing
the report.

## Scan sequence

1. Discover all boards and projects visible to the connected Jira account.
2. Read active sprint issues where available.
3. Include open, in-progress, blocked, review and recently completed issues.
4. Capture issue key, summary, status, assignee, priority, sprint, updated time,
   due date, blockers/dependencies, comments and linked pull requests when
   available.
5. Record inaccessible boards, permission errors and empty responses separately.
6. Use a seven-day activity window by default; fall back to fourteen days when
   no active sprint exists.

## Follow-up rules

Flag an issue for a status question when one or more of these is true:

- It is blocked or has an unresolved dependency.
- It is overdue or its target date is at risk.
- It has no assignee or no recent activity.
- Its status has not changed during the reporting window.
- It is in review without linked review evidence.
- Its acceptance criteria or next step is unclear.

Do not label work stale without showing the last-updated timestamp and the
threshold used. Do not infer that an assignee is the current owner if Jira
returns no assignee.

## Report format

Return:

- scan timestamp and reporting window;
- boards/projects scanned and coverage gaps;
- aggregate counts by board and status;
- completed, active, blocked, stale, overdue and unassigned highlights;
- one concise status-question set per owner/issue;
- decisions or escalations needed from the PO;
- verification limits.

Questions should be actionable and issue-specific:

1. What changed since the last update?
2. What is the next concrete deliverable and expected date?
3. What is blocked or dependent on another person/team?
4. Is the current status and target date still accurate?

Always preserve Jira issue keys and direct links. Cite the returned Jira data for
each claim, and distinguish observations from recommendations.
