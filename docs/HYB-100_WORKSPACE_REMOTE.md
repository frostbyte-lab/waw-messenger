# HYB-100 — Workspace Remote

## Security model

Workspace Remote berjalan hanya dari WAW penuh sebagai operator/admin dan Host Companion ringan pada perangkat target. Target menerima invite yang one-time dan expiry-bound. Session tidak aktif sebelum target memberikan consent eksplisit untuk capability yang diminta.

## Consent and revoke

Capability `screen`, `touch`, `keyboard`, `file_transfer`, dan `approved_actions` diminta secara terpisah. Target dapat menolak atau mencabut consent. Revoke harus menghentikan relay room, media projection, accessibility-driven actions, foreground service, file transfer, heartbeat, dan session token. Emergency disconnect mengakhiri session di kedua sisi.

## Relay

Relay memakai WSS melalui Cloudflare Tunnel. APK tidak menyimpan token Cloudflare. Room ID, session token, dan nonce bersifat scoped, short-lived, tidak dapat digunakan kembali setelah redeem atau expiry, dan tidak boleh ditulis ke log biasa.

## Android boundary

Screen sharing memerlukan MediaProjection consent dari sistem Android. Touch, approved actions, dan text input memakai Accessibility Service sesuai permission Android. App target tetap melihat indikator native dan foreground notification selama service berjalan. File transfer memakai media pipeline dan batas policy yang terpisah.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Invite | One-time, expiry-bound, tenant/session-scoped, tidak dapat dipakai ulang |
| Consent | Target menyetujui capability secara eksplisit; reject tidak mengaktifkan service |
| Relay | WSS room isolation dan no Cloudflare token in APK |
| Revoke | Menghentikan semua service, media projection, relay, token, transfer, dan heartbeat |
| Actions | Hanya approved allowlist: Back, Home, Recents; semua diaudit |
| Session | Heartbeat, reconnect, timeout, ended, failed, dan revoked state tersedia |
| Android | Foreground notification permanen dan native permission indicator aktif |
| Audit | Invite, consent, action, transfer, revoke, disconnect tercatat |
| Isolation | Operator tidak dapat mengakses target di luar tenant/workspace/session |
