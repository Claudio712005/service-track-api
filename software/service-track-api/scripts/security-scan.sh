#!/bin/bash

set -e

echo "Running SAST (Semgrep)..."

docker run --rm \
  -v "$(pwd):/src" \
  semgrep/semgrep \
  semgrep --config=auto --json --output /src/reports/semgrep-report.json

echo "Running SCA (Dependency Check)..."

docker run --rm \
  -v "$(pwd):/src" \
  owasp/dependency-check:latest \
  --scan /src \
  --format JSON \
  --out /src/reports

echo "Security scan finished"