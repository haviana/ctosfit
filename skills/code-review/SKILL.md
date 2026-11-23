# Skill: Independent Code Review

1. Read the issue/acceptance criteria and relevant architecture/product docs.
2. Inspect the full diff and surrounding code.
3. Check correctness, security, tenant boundaries, data integrity and failure modes first.
4. Verify tests cover behavior rather than implementation details.
5. Check migrations, API compatibility, observability and rollback implications.
6. Produce findings with severity, file/line, impact and actionable remediation.
7. State verification limits and checks not run.

Do not approve a change you authored. Do not merge or waive required checks.
