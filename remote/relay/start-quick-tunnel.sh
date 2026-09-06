#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

if ! command -v cloudflared >/dev/null 2>&1; then
  echo "cloudflared belum terpasang." >&2
  echo "Install: https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/" >&2
  exit 1
fi

if command -v docker >/dev/null 2>&1; then
  docker compose up -d --build relay
else
  echo "Docker tidak tersedia; menjalankan relay langsung dengan Node.js."
  npm ci --omit=dev
  PORT="${PORT:-8787}" node server.js >/tmp/waw-relay.log 2>&1 &
  RELAY_PID=$!
  trap 'kill "$RELAY_PID" 2>/dev/null || true' EXIT INT TERM
fi

echo "Relay lokal aktif di http://127.0.0.1:8787"
echo "Membuka Cloudflare Quick Tunnel..."
echo "Salin URL https://....trycloudflare.com dari output, lalu gunakan sebagai wss://....trycloudflare.com pada APK."
exec cloudflared tunnel --no-autoupdate --url http://127.0.0.1:8787
