# WAW Hybrid — Urutan Pengerjaan GitHub

Dokumen ini adalah urutan kerja resmi. Setiap milestone mengikuti: **DESIGN → IMPLEMENT → BUILD → TEST → SECURITY REVIEW → DOCUMENT → LOCK**. Jangan menandai milestone selesai tanpa bukti build/test dan acceptance criteria.

## Milestone 0 — Baseline dan Proteksi Repository

- [ ] Pastikan branch kerja berbasis `origin/main` terbaru.
- [ ] Tambahkan `.env.example` tanpa secret.
- [ ] Tetapkan CODEOWNERS, branch protection, CI, dan status check.
- [ ] Simpan referensi UI WAW dan keputusan official WhatsApp boundary.

**Selesai jika:** CI berjalan, secret scan lulus, dan perubahan dapat direview melalui Pull Request.

## Milestone 1 — Contracts dan Data Boundary

- [ ] Finalisasi enum `waw_internal` dan `whatsapp_business`.
- [ ] Finalisasi DTO User, Tenant, Workspace, Thread, Message, Media, RemoteSession.
- [ ] Finalisasi realtime event dan error code.
- [ ] Tambahkan schema validation dan API versioning.
- [ ] Tetapkan retention, deletion, audit, dan idempotency rules.

**Selesai jika:** frontend/backend memakai package contracts yang sama dan schema test lulus.

## Milestone 2 — Auth, Tenant, Role, dan Audit

- [ ] Auth session WAW, refresh token, MFA, logout, dan recovery.
- [ ] Tenant/workspace membership dan role guard.
- [ ] API request context tidak boleh menerima tenant dari client tanpa verifikasi.
- [ ] Audit perubahan role, assignment, export, webhook, dan remote access.
- [ ] Rate limit dan abuse protection.

**Selesai jika:** test cross-tenant access selalu gagal dan audit dapat ditelusuri dengan request ID.

## Milestone 3 — API Gateway dan Database

- [ ] Health/readiness endpoint.
- [ ] Migration untuk tenant, user, workspace, thread, message, media, audit, webhook event.
- [ ] Repository layer dengan transaction boundary.
- [ ] Pagination, filtering, optimistic concurrency, dan error envelope.
- [ ] Backup/restore procedure tanpa secret di migration.

**Selesai jika:** migration fresh/rollback/restore diuji dan API contract test lulus.

## Milestone 4 — UI WAW Web Parity

- [ ] Pecah UI referensi menjadi auth, inbox, thread, contacts, status, calls, workspace, settings, notifications.
- [ ] Gunakan `packages/ui` untuk tokens, shell, bubble, composer, cards, dialogs, toast.
- [ ] Pertahankan label `WA Hybrid`, `WAW Workspace`, dan channel badge.
- [ ] Pastikan responsive mobile/desktop dan accessibility keyboard.
- [ ] Tambahkan loading, empty, error, offline, retry, dan optimistic states.

**Selesai jika:** screenshot review menunjukkan parity visual terhadap referensi WAW dan browser smoke test lulus.

**Visual contract wajib:** referensi https://waw-messenger-api.technologiesfrostbyte.workers.dev/ berlaku untuk web dan APK. Screenshot review harus mencakup desktop web, mobile web, APK WAW, Workspace, Workspace Remote, loading, empty, error, dan consent state. Dialog permission Android dan layar native boleh berbeda hanya karena batas platform.

## Milestone 5 — WAW Internal Chat dan Status

- [ ] Thread/message/reaction/read receipt internal.
- [ ] Presence dan typing melalui realtime gateway.
- [ ] Status WAW dengan visibility, expiry, media, dan deletion.
- [ ] Notifications dan unread counters.
- [ ] Search hanya pada data tenant yang diizinkan.

**Selesai jika:** dua user tenant yang sama dapat berkomunikasi dan tenant berbeda tidak dapat melihat data.

## Milestone 6 — WhatsApp Business Cloud Adapter

- [ ] Konfigurasi nomor bisnis, Graph API version, dan secret server.
- [ ] Send text/template/media melalui adapter terisolasi.
- [ ] Mapping inbound message/media/status ke model `whatsapp_business`.
- [ ] Webhook signature verification.
- [ ] Idempotency, retry, dead-letter queue, dan delivery status.
- [ ] Retention dan deletion untuk data bisnis.

**Selesai jika:** sandbox/test number resmi Meta berhasil send/receive/webhook tanpa credential masuk client atau log.

## Milestone 7 — Media Pipeline

- [ ] Presigned upload/download.
- [ ] Object storage boundary dan metadata.
- [ ] Virus scan/quarantine.
- [ ] Thumbnail, size/type validation, retention, delete.
- [ ] Watermark opsional untuk output WAW.

**Selesai jika:** upload besar, file invalid, blocked file, expiry URL, dan deletion diuji.

## Milestone 8 — Calls dan Realtime Signaling

- [ ] WebSocket gateway untuk presence/typing/receipts.
- [ ] WebRTC signaling internal WAW.
- [ ] STUN/TURN configuration melalui secret manager.
- [ ] Consent microphone/camera, active indicator, emergency hangup.
- [ ] WhatsApp Business voice calling hanya jika account/API eligibility dikonfirmasi.

**Selesai jika:** call internal berhasil pada dua jaringan berbeda dan permission/revoke/error state teruji.

## Milestone 9 — Workspace Integration

- [ ] Migrasikan file manager, documents/PDF, scanner, watermark, vault, diagnostics, shield, notes, backup, search.
- [ ] Tenant/workspace authorization untuk setiap resource.
- [ ] Upload media memakai media service, bukan API chat biasa.
- [ ] Audit export, restore, file sharing, dan sensitive actions.

**Selesai jika:** seluruh Workspace muncul dalam shell WAW Hybrid dan smoke test setiap tool lulus.

## Milestone 10 — Workspace Remote

- [ ] Admin/operator hanya dari WAW penuh.
- [ ] Link invite untuk user target dan Host Companion ringan.
- [ ] Token one-time/expiry, room isolation, explicit consent, revoke.
- [ ] Cloudflare Tunnel WSS ke relay tanpa token Cloudflare di APK.
- [ ] MediaProjection, Accessibility, foreground service, file transfer, heartbeat, reconnect.
- [ ] Audit session lifecycle dan emergency disconnect.

**Selesai jika:** remote hanya berjalan setelah consent target; revoke memutuskan sesi; token expired tidak dapat dipakai ulang; dua perangkat dan dua jaringan berhasil diuji.

## Milestone 11 — Security, Observability, dan Release

- [ ] Threat model dan dependency scan.
- [ ] Secret scan, SAST, DAST, rate-limit test, tenant isolation test.
- [ ] Structured logs tanpa credential/message sensitive payload.
- [ ] Metrics: latency, error rate, webhook lag, queue depth, WebRTC success.
- [ ] Backup restore drill dan incident runbook.
- [ ] Staging release, manual QA, Android/web/browser matrix, rollback plan.

**Selesai jika:** release checklist, security review, monitoring, rollback, dan documentation review disetujui.

## Urutan GitHub Issue yang Disarankan

Buat issue/PR secara berurutan menggunakan prefix berikut:

| Prefix | Issue |
|---|---|
| `HYB-000` | Baseline, CI, secret scan, branch protection |
| `HYB-010` | Contracts dan channel schema |
| `HYB-020` | Auth, tenant guard, role, audit |
| `HYB-030` | Database dan API gateway |
| `HYB-040` | UI shell dan design system parity |
| `HYB-050` | WAW internal chat/status/realtime |
| `HYB-060` | WhatsApp Business adapter/webhook |
| `HYB-070` | Media pipeline |
| `HYB-080` | WebRTC dan calling eligibility |
| `HYB-090` | Workspace integration |
| `HYB-100` | Workspace Remote + Cloudflare Tunnel |
| `HYB-110` | Security, observability, release |

Setiap issue harus berisi **Scope**, **Out of scope**, **Dependencies**, **Acceptance criteria**, **Test evidence**, **Security notes**, dan **Rollback/cleanup**.
