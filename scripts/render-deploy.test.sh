#!/usr/bin/env bash
# Exercise the Render deploy script without calling Render.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
script="$root/scripts/render-deploy.sh"
sha="0123456789abcdef0123456789abcdef01234567"
api_hook="https://hooks.example.test/api-hook-secret-value"
web_hook="https://hooks.example.test/web-hook-secret-value?key=web-secret"

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT
cat > "$tmpdir/curl" <<'EOF'
#!/usr/bin/env bash
printf '%s\n' "${@: -1}" >> "${CURL_LOG:?}"
printf '%s' "${CURL_HTTP_CODE:-200}"
EOF
chmod +x "$tmpdir/curl"

assert_secret_hidden() {
  local output="$1"
  local visible
  visible="$(printf '%s\n' "$output" | grep -v '^::add-mask::' || true)"
  if printf '%s\n' "$visible" | grep -F "api-hook-secret-value" >/dev/null; then
    echo "API hook URL leaked to the log" >&2
    exit 1
  fi
  if printf '%s\n' "$visible" | grep -F "web-hook-secret-value" >/dev/null || printf '%s\n' "$visible" | grep -F "web-secret" >/dev/null; then
    echo "Web hook URL leaked to the log" >&2
    exit 1
  fi
}

run_deploy() {
  local code=0
  CURL_LOG="$tmpdir/curl.log" PATH="$tmpdir:$PATH" "$@" >"$tmpdir/out" 2>"$tmpdir/err" || code=$?
  cat "$tmpdir/out" "$tmpdir/err"
  return "$code"
}

: >"$tmpdir/curl.log"
set +e
output="$(run_deploy env -u RENDER_DEPLOY_HOOK_API -u RENDER_DEPLOY_HOOK_WEB bash "$script" "$sha")"
status=$?
set -e
[[ "$status" -ne 0 ]]
printf '%s\n' "$output" | grep -F "Missing GitHub Actions secret RENDER_DEPLOY_HOOK_API" >/dev/null
[[ ! -s "$tmpdir/curl.log" ]]

: >"$tmpdir/curl.log"
set +e
output="$(run_deploy env -u RENDER_DEPLOY_HOOK_WEB RENDER_DEPLOY_HOOK_API="$api_hook" bash "$script" "$sha")"
status=$?
set -e
[[ "$status" -ne 0 ]]
printf '%s\n' "$output" | grep -F "Missing GitHub Actions secret RENDER_DEPLOY_HOOK_WEB" >/dev/null
[[ ! -s "$tmpdir/curl.log" ]]

: >"$tmpdir/curl.log"
set +e
output="$(run_deploy env RENDER_DEPLOY_HOOK_API="http://hooks.example.test/nope" RENDER_DEPLOY_HOOK_WEB="$web_hook" bash "$script" "$sha")"
status=$?
set -e
[[ "$status" -ne 0 ]]
printf '%s\n' "$output" | grep -F "RENDER_DEPLOY_HOOK_API must be a single-line https URL" >/dev/null
assert_secret_hidden "$output"

: >"$tmpdir/curl.log"
output="$(run_deploy env RENDER_DEPLOY_HOOK_API="$api_hook" RENDER_DEPLOY_HOOK_WEB="$web_hook" CURL_HTTP_CODE=201 bash "$script" "$sha")"
assert_secret_hidden "$output"
printf '%s\n' "$output" | grep -F "ctosfit-api-dev (HTTP 201)" >/dev/null
printf '%s\n' "$output" | grep -F "ctosfit-web-dev (HTTP 201)" >/dev/null
grep -F "${api_hook}?ref=${sha}" "$tmpdir/curl.log" >/dev/null
grep -F "${web_hook}&ref=${sha}" "$tmpdir/curl.log" >/dev/null

: >"$tmpdir/curl.log"
set +e
output="$(run_deploy env RENDER_DEPLOY_HOOK_API="$api_hook" RENDER_DEPLOY_HOOK_WEB="$web_hook" CURL_HTTP_CODE=500 bash "$script" "$sha")"
status=$?
set -e
[[ "$status" -ne 0 ]]
printf '%s\n' "$output" | grep -F "ctosfit-api-dev returned HTTP 500" >/dev/null
assert_secret_hidden "$output"
[[ "$(wc -l < "$tmpdir/curl.log")" -eq 1 ]]

echo "render deploy script checks passed"
