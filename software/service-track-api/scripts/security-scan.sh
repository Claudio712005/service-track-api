#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
REPORTS_DIR="$PROJECT_DIR/reports"
NVD_CACHE_DIR="${HOME}/.owasp/data"

mkdir -p "$REPORTS_DIR" "$NVD_CACHE_DIR"

echo "==> [1/2] SAST — Semgrep"
docker run --rm \
  -v "$PROJECT_DIR:/src" \
  -w /src \
  semgrep/semgrep:latest \
  semgrep --config=auto \
          --sarif \
          --output /src/reports/semgrep.sarif \
          /src &
SEMGREP_PID=$!

echo "==> [2/2] SCA — OWASP Dependency Check"
docker run --rm \
  -v "$PROJECT_DIR:/src" \
  -v "$NVD_CACHE_DIR:/data" \
  owasp/dependency-check:latest \
  --scan /src \
  --format JSON \
  --out /src/reports \
  --data /data \
  --failOnCVSS 11 \
  --project "service-track-api" &
DC_PID=$!

wait_or_fail() {
    local pid=$1 name=$2
    if ! wait "$pid"; then
        echo "ERROR: $name failed"
        return 1
    fi
    echo "  $name done"
}

wait_or_fail $SEMGREP_PID "Semgrep"
wait_or_fail $DC_PID "Dependency Check"

echo "==> Converting dependency-check JSON → SARIF"
node "$SCRIPT_DIR/convert-to-sarif.js" "$REPORTS_DIR"

echo "==> Reports:"
ls -lh "$REPORTS_DIR"
