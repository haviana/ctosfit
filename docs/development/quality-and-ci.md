# Quality and CI

GitHub Actions checks pull requests and pushes. The CI workflow targets pull requests to `main` and pushes to `main`; the quality workflow also checks repository hygiene and conditionally runs backend and frontend checks when those projects are present.

The backend uses JDK 25 and the Gradle Wrapper pinned to Gradle 9.7.0. CI runs `./gradlew clean test build`; the quality workflow runs `./gradlew check` for Gradle projects that include the wrapper.

Tenant-isolation tests are part of that Gradle test run. They start PostgreSQL 17 with Testcontainers, so the CI runner must have a working Docker daemon. Both workflows check Docker before the build and, after a successful test run, require the JUnit reports to match exactly: 26 tests in `PostgreSqlTenantIsolationTest` and 5 in `ExerciseTenantIsolationIntegrationTest`, with no failures, errors, or skips. A different count fails the build, so a new case has to update `scripts/require-tenant-isolation-results.sh` in the same change. A focused rerun is:

```bash
./gradlew :app:test -PincludeTags=tenant-isolation
```

An `includeTags` run that matches nothing fails `:app:test`.

Keep CI reproducible and fast. Add static analysis, dependency scanning, coverage thresholds and architecture tests as separate, justified changes rather than silently weakening the build.
