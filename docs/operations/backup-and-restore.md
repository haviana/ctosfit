# Backup and restore

The production database is PostgreSQL. Backups use PostgreSQL's custom dump format so they can be encrypted and restored selectively.

## Create a backup

```bash
CTOSFIT_DATABASE_URL='postgresql://user:password@host:5432/ctosfit' \
  ./scripts/backup-restore.sh backups/ctosfit-$(date -u +%Y%m%dT%H%M%SZ).dump
```

The dump must be stored outside the application host, encrypted at rest, and retained according to the deployment's recovery-point objective. Never commit a dump or credentials.

## Demonstrate a restore

Restore into an isolated empty database, never over the live database:

```bash
./scripts/backup-restore.sh backups/ctosfit-example.dump \
  --restore-to 'postgresql://ctosfit:ctosfit@localhost:5432/ctosfit_restore'
psql 'postgresql://ctosfit:ctosfit@localhost:5432/ctosfit_restore' \
  -c 'SELECT COUNT(*) FROM boxes;'
```

The restore is successful only when PostgreSQL accepts the dump and the expected tenant tables and row counts are present. Run this procedure before a pilot and after every migration that changes the schema.
