set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SONAR_URL="${SONAR_HOST_URL:-http://localhost:9000}"
MAX_WAIT=120

cd "$PROJECT_DIR"

echo "==> Starting SonarQube stack..."
docker compose -f docker-compose.sonar.yml up -d

echo "==> Waiting for SonarQube at $SONAR_URL (max ${MAX_WAIT}s)..."
elapsed=0
until curl -sf "$SONAR_URL/api/system/status" | grep -q '"status":"UP"'; do
    if (( elapsed >= MAX_WAIT )); then
        echo "ERROR: SonarQube did not start within ${MAX_WAIT}s"
        exit 1
    fi
    sleep 5
    (( elapsed += 5 ))
    echo "  ...${elapsed}s"
done
echo "==> SonarQube is up."

echo "==> Running: clean check sonar"
./gradlew clean check sonar \
    -Dsonar.host.url="$SONAR_URL" \
    ${SONAR_TOKEN:+-Dsonar.login="$SONAR_TOKEN"}
