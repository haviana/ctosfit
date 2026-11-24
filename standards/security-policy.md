# Secure engineering baseline

- Apply least privilege to users, services, CI jobs and tokens.
- Store secrets only in approved secret managers or protected CI variables.
- Authenticate identities and authorize every sensitive operation server-side.
- Validate input, constrain resource consumption and use safe output encoding.
- Use TLS for network traffic and approved encryption for sensitive stored data.
- Keep dependencies current; scan for vulnerabilities and review licenses.
- Protect against injection, SSRF, path traversal, insecure deserialization and common web risks.
- Avoid sensitive data in logs, traces, test fixtures and error messages.
- Define retention, deletion and access controls for personal or regulated data.
- Require threat assessment and human approval for high-risk security changes.
