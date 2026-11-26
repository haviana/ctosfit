# Ctosfit Skills

Task playbooks that agents can follow. Read the root `AGENTS.md` and relevant standards first.

- [Feature delivery](feature-delivery/SKILL.md)
- [Secure tenant-scoped change](secure-tenant-change/SKILL.md)
- [Independent code review](code-review/SKILL.md)
- [Jira status monitor](jira-status-monitor/SKILL.md)
- [Jira workflow automation](jira-workflow/SKILL.md)
- [Jira ticket delivery factory](jira-ticket-delivery-factory/SKILL.md)
- [Secure change](secure-change/SKILL.md)

Use the factory with an explicit Jira key for one ticket or `next` for one
configured backlog claim. Parallel batches use explicit issue keys and separate
Git worktrees; they do not call single-ticket `next` repeatedly.
