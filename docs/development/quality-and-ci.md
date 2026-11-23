# Quality and CI

GitHub Actions checks pull requests and pushes. The CI workflow targets pull requests to `main` and pushes to `main`; the quality workflow also checks repository hygiene and conditionally runs backend and frontend checks when those projects are present.

The backend uses JDK 25 and the Gradle Wrapper pinned to Gradle 9.7.0. CI runs `./gradlew clean test build`; the quality workflow runs `./gradlew check` for Gradle projects that include the wrapper.

Keep CI reproducible and fast. Add static analysis, dependency scanning, coverage thresholds and architecture tests as separate, justified changes rather than silently weakening the build.
