#!/usr/bin/env bash
# Fail when the PostgreSQL tenant-isolation suites did not actually run.
set -euo pipefail

results_dir="${1:-app/build/test-results/test}"

python3 - "$results_dir" <<'PY'
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

results = Path(sys.argv[1])
# Exact JUnit test counts. Lowering either number hides a deleted case.
# PostgreSqlTenantIsolationTest: exercises, workout drafts, publications, results,
# memberships, inactive memberships, athlete-token versus owner/coach membership,
# selected-box header, plus roleBoundaries() invocations.
# ExerciseTenantIsolationIntegrationTest: box onboarding, owner retention, repository
# owner/reactivation, exercise isolation, publication and result path.
required = {
    "TEST-com.ctosfit.security.PostgreSqlTenantIsolationTest.xml": 26,
    "TEST-com.ctosfit.security.ExerciseTenantIsolationIntegrationTest.xml": 5,
}
total = 0
for name, expected in required.items():
    path = results / name
    if not path.is_file():
        print(f"missing tenant-isolation result: {path}", file=sys.stderr)
        sys.exit(1)
    root = ET.parse(path).getroot()
    tests = int(root.attrib.get("tests", "0"))
    failures = int(root.attrib.get("failures", "0"))
    errors = int(root.attrib.get("errors", "0"))
    skipped = int(root.attrib.get("skipped", "0"))
    print(f"{name}: tests={tests} failures={failures} errors={errors} skipped={skipped}")
    if failures or errors or skipped or tests != expected:
        print(f"{name} did not meet the tenant-isolation gate (expected {expected} tests)", file=sys.stderr)
        sys.exit(1)
    total += tests
print(f"tenant-isolation tests executed: {total}")
PY
