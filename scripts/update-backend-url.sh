#!/bin/bash
# Updates verev.backend.baseUrl in local.properties.
# Run after changing WiFi (physical device) or use --emulator for Android emulator.
#
# Usage:
#   ./scripts/update-backend-url.sh           # Use current WiFi IP (physical device)
#   ./scripts/update-backend-url.sh --emulator   # Use 10.0.2.2 (emulator only)

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCAL_PROPERTIES="$PROJECT_ROOT/local.properties"

if [ "$1" = "--emulator" ]; then
  BACKEND_URL="http://10.0.2.2:8080"
  echo "Using emulator URL (10.0.2.2 = host from emulator)"
else
  # Get current IP (WiFi: en0 on Mac, fallback to first non-loopback)
  IP=$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || \
    (ifconfig 2>/dev/null | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1))
  if [ -z "$IP" ]; then
    echo "Could not detect your IP. Set verev.backend.baseUrl manually in local.properties"
    exit 1
  fi
  BACKEND_URL="http://${IP}:8080"
  echo "Detected IP: $IP (phone and Mac must be on same WiFi)"
fi

# Ensure sdk.dir exists
if [ ! -f "$LOCAL_PROPERTIES" ]; then
  echo "Creating local.properties..."
  echo "sdk.dir=$HOME/Library/Android/sdk" > "$LOCAL_PROPERTIES"
  echo "verev.backend.baseUrl=$BACKEND_URL" >> "$LOCAL_PROPERTIES"
else
  if grep -q "^verev.backend.baseUrl=" "$LOCAL_PROPERTIES"; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
      sed -i '' "s|^verev.backend.baseUrl=.*|verev.backend.baseUrl=$BACKEND_URL|" "$LOCAL_PROPERTIES"
    else
      sed -i "s|^verev.backend.baseUrl=.*|verev.backend.baseUrl=$BACKEND_URL|" "$LOCAL_PROPERTIES"
    fi
  else
    echo "verev.backend.baseUrl=$BACKEND_URL" >> "$LOCAL_PROPERTIES"
  fi
fi

echo "Set verev.backend.baseUrl=$BACKEND_URL"
echo ""
echo "IMPORTANT: Rebuild the app (URL is baked in at build time):"
echo "  cd VerevCodex && ./gradlew :app:assembleDebug"
echo ""
echo "Also ensure:"
echo "  - Backend is running: cd VerevBackend && ./gradlew bootRun"
echo "  - Postgres/Redis: cd VerevBackend && docker compose up -d postgres redis"
