#!/usr/bin/env bash
# POST the Render deploy hooks for the development API and web services.
# The hook URLs are credentials. This script masks them and never prints them.
set -euo pipefail

sha="${1:-}"
if [[ ! "$sha" =~ ^[0-9a-fA-F]{40}$ ]]; then
  echo "::error::Refusing to deploy because the commit SHA is missing or not a 40-character hex value."
  exit 1
fi

require_secret() {
  local name="$1"
  local value="${2:-}"
  if [[ -z "$value" ]]; then
    echo "::error::Missing GitHub Actions secret ${name}. Add it under Settings > Secrets and variables > Actions. In Render, open the service and copy Settings > Deploy Hook into that secret."
    exit 1
  fi
  if [[ "$value" != https://* || "$value" == *$'\n'* || "$value" == *$'\r'* || "$value" == *' '* ]]; then
    echo "::error::GitHub Actions secret ${name} must be a single-line https URL from Settings > Deploy Hook. Replace the secret value."
    exit 1
  fi
  printf '::add-mask::%s\n' "$value"
}

append_ref() {
  local url="$1"
  local ref="$2"
  local last="${url: -1}"
  if [[ "$url" != *'?'* ]]; then
    printf '%s?ref=%s' "$url" "$ref"
  elif [[ "$last" == '?' || "$last" == '&' ]]; then
    printf '%sref=%s' "$url" "$ref"
  else
    printf '%s&ref=%s' "$url" "$ref"
  fi
}

post_hook() {
  local service="$1"
  local secret_name="$2"
  local hook="$3"
  local target body_file err_file status
  target="$(append_ref "$hook" "$sha")"
  printf '::add-mask::%s\n' "$target"
  body_file="$(mktemp)"
  err_file="$(mktemp)"
  # curl includes the request URL in its own error text. Keep that off the log.
  status="$(curl --silent --output "$body_file" --write-out '%{http_code}' --request POST --max-time 60 --proto '=https' "$target" 2>"$err_file" || true)"
  rm -f "$body_file" "$err_file"
  if [[ ! "$status" =~ ^[0-9]{3}$ || "$status" -lt 200 || "$status" -ge 300 ]]; then
    if [[ -z "$status" || "$status" == "000" ]]; then
      echo "::error::Render deploy hook for ${service} failed before receiving an HTTP status. Check the ${secret_name} secret and the Render service."
    else
      echo "::error::Render deploy hook for ${service} returned HTTP ${status}."
    fi
    exit 1
  fi
  echo "Render accepted the deploy hook for ${service} (HTTP ${status})."
}

require_secret RENDER_DEPLOY_HOOK_API "${RENDER_DEPLOY_HOOK_API:-}"
require_secret RENDER_DEPLOY_HOOK_WEB "${RENDER_DEPLOY_HOOK_WEB:-}"
post_hook "ctosfit-api-dev" "RENDER_DEPLOY_HOOK_API" "$RENDER_DEPLOY_HOOK_API"
post_hook "ctosfit-web-dev" "RENDER_DEPLOY_HOOK_WEB" "$RENDER_DEPLOY_HOOK_WEB"
