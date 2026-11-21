# Quality and CI

GitHub Actions runs on pull requests and pushes to `main`:
1. Checkout source.
2. Install JDK 21.
3. Provision Gradle 8.14.1.
4. Run `gradle clean test build`.

Keep CI reproducible and fast. Add static analysis, dependency scanning, coverage thresholds and architecture tests as separate, justified changes rather than silently weakening the build.
