#!/bin/bash
set -euo pipefail

echo "Backup service started"

INTERVAL="${BACKUP_INTERVAL_SECONDS:-86400}"
RETENTION="${BACKUP_RETENTION_DAYS:-5}"

# Verificar variables obligatorias
: "${MYSQL_USER:?Variable MYSQL_USER no definida}"
: "${MYSQL_PWD:?Variable MYSQL_PWD no definida}"
: "${MYSQL_DATABASE:?Variable MYSQL_DATABASE no definida}"
: "${R2_BACKUP_BUCKET:?Variable R2_BACKUP_BUCKET no definida}"
: "${R2_ENDPOINT:?Variable R2_ENDPOINT no definida}"
: "${R2_BACKUP_PREFIX:?Variable R2_BACKUP_PREFIX no definida}"

while true; do
  TS=$(date +"%Y-%m-%d_%H-%M-%S")
  FILE="/backups/backup_${TS}.sql.gz"

  echo "$(date) - Creating backup ${TS}"

  # Dump MySQL
  if ! mysqldump \
    -h dcava-db \
    -u "$MYSQL_USER" \
    --password="$MYSQL_PWD" \
    --single-transaction \
    --quick \
    --lock-tables=false \
    "$MYSQL_DATABASE" | gzip > "$FILE"; then
      echo "$(date) - ERROR: MySQL dump failed"
      sleep "$INTERVAL"
      continue
  fi

  # Verificar dump
  if ! gzip -t "$FILE"; then
    echo "$(date) - ERROR: Dump corrupt"
    rm -f "$FILE"
    sleep "$INTERVAL"
    continue
  fi

  echo "$(date) - Uploading backup to R2"
  if ! aws s3 cp "$FILE" \
    "s3://${R2_BACKUP_BUCKET}/${R2_BACKUP_PREFIX}/$(basename "$FILE")" \
    --endpoint-url "$R2_ENDPOINT"; then
      echo "$(date) - ERROR: Upload to R2 failed"
  fi

  echo "$(date) - Cleaning old local backups"
  find /backups -type f -mtime +"$RETENTION" -delete

  echo "$(date) - Backup completed, sleeping ${INTERVAL}s"
  sleep "$INTERVAL"
done

