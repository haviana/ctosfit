#!/usr/bin/env bash
set -euo pipefail

: "${CTOSFIT_DATABASE_URL:=postgresql://ctosfit:ctosfit@localhost:5432/ctosfit}"
backup_file="${1:-backups/ctosfit-$(date -u +%Y%m%dT%H%M%SZ).dump}"

mkdir -p "$(dirname "$backup_file")"
pg_dump --format=custom --no-owner --file="$backup_file" "$CTOSFIT_DATABASE_URL"
echo "Backup written to $backup_file"

if [[ "${2:-}" == "--restore-to" ]]; then
  target_url="${3:?usage: $0 [backup-file] --restore-to postgres-url}"
  createdb "$target_url" 2>/dev/null || true
  pg_restore --clean --if-exists --no-owner --dbname="$target_url" "$backup_file"
  echo "Backup restored to $target_url"
fi
