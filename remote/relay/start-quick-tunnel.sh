#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

if ! command -v cloudflared >/dev/null 2>&1; then
  echo "cloudflared belum terpasang." >&2
  echo "Install: https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker belum terpasang." >&2
  exit 1
fi

docker compose up -d --build relay

echo "Relay lokal aktif di http://127.0.0.1:8787"
echo "Membuka Cloudflare Quick Tunnel..."
echo "Salin URL https://....trycloudflare.com dari output, lalu gunakan sebagai wss://....trycloudflare.com pada APK."
exec cloudflared tunnel --no-autoupdate --url http://127.0.0.1:8787
