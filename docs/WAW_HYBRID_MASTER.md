# WAW Hybrid — Master Product & Architecture Specification

## Tujuan

WAW Hybrid memiliki satu UI produk WAW dengan dua jalur komunikasi yang tetap terpisah di backend: `waw_internal` untuk akun, chat, Status, workspace, WebRTC, kontak, notifikasi, dan audit WAW; serta `whatsapp_business` untuk WhatsApp Business Cloud API melalui jalur resmi Meta.

UI harus tetap 100% WAW. Percakapan WhatsApp Business dan komunikasi internal WAW memakai komponen UI yang sama, tetapi setiap data membawa channel, tenant, workspace, role, retention, dan audit boundary yang sesuai.

## Batas Resmi dan Privasi

WAW tidak mengambil alih login personal WhatsApp, password, cookie, QR token personal, session key, atau UI WhatsApp Web internal. Integrasi WhatsApp hanya melalui API resmi Meta untuk nomor bisnis yang terdaftar dan pelanggan sesuai kebijakan Meta. Data chat personal dan Status personal tidak diproses.

Credential Meta hanya boleh berada di secret manager/server. Token provider tidak boleh masuk frontend, `localStorage`, log, commit, APK, atau artefak Pages.

## Struktur Target

```text
waw-hybrid/
├── apps/web                 # UI WAW: auth, inbox, thread, contacts, status, calls, workspace, settings, notifications
├── apps/api                 # API gateway, auth, tenant guard, workspace, webhook, health
├── packages/contracts       # DTO, enum channel, event schema, validation, versioning
├── packages/ui              # design system WAW reusable
├── services/whatsapp        # Graph API adapter, webhook verify, mapping, templates, delivery, calling
├── services/realtime        # WebSocket, presence, typing, receipts, WebRTC signaling
├── services/media           # presign, storage, scan, thumbnail, watermark, retention, signed URL
├── docs                     # architecture, API, security, decisions, runbooks
└── infra                    # migrations, containers, deployment, monitoring, backup; no real secrets
```

## Model Channel

| Channel | Sumber | Data yang boleh diproses |
|---|---|---|
| `waw_internal` | Backend WAW | User, tenant, workspace, chat internal, Status WAW, WebRTC, assignment, audit |
| `whatsapp_business` | Meta WhatsApp Business Cloud API | Pesan bisnis, media bisnis, webhook delivery, template, calling jika eligible |

Event minimal membawa `id`, `tenantId`, `workspaceId`, `channel`, `type`, `externalId`, `occurredAt`, dan `payload`. Webhook wajib idempotent berdasarkan `externalId` dan event type; kegagalan dipindahkan ke dead-letter queue dengan audit payload yang diperlukan.

## UI Wajib dan Visual Contract

Tampilan WAW pada deployment referensi [WAW Messenger](https://waw-messenger-api.technologiesfrostbyte.workers.dev/) menjadi **visual contract wajib**. Web dan APK tidak boleh memiliki shell produk yang berbeda secara material. URL tersebut adalah referensi/entry point UI, bukan tempat menyimpan secret dan bukan otomatis endpoint API atau WSS.

Persyaratan ini berlaku untuk `apps/web`, APK WAW Hybrid, WAW Workspace, Workspace Remote, dan Host Companion. Semua permukaan wajib mempertahankan branding WAW, shell navigasi, hirarki halaman, warna, typography, spacing, card, bubble, status indicator, button treatment, responsive behavior, serta animasi utama. Perbedaan hanya diperbolehkan jika dipaksa oleh platform, seperti system permission dialog Android, MediaProjection consent, Accessibility settings, file picker, atau safe-area/native back behavior.

Setiap perubahan UI wajib menyertakan screenshot review untuk desktop web, mobile web, dan APK. Acceptance test visual harus membandingkan halaman entry, inbox/chat, Workspace, Workspace Remote, loading, empty, error, dan consent state terhadap referensi. Jika parity belum tercapai, milestone UI berstatus `IN_PROGRESS`, bukan `LOCKED`.

Design system memakai WAW green/teal, blue secondary accent, white surface, compact typography, rounded cards, responsive layout, smooth navigation, message status transitions, attachment progress, dan Workspace sebagai tujuan utama.

## Remote Workspace

Workspace Remote tetap merupakan fitur di dalam WAW penuh untuk admin/operator. User target menerima link undangan dan dapat memasang Host Companion ringan bila diperlukan. Sesi wajib explicit, memiliki token expiry, revoke, audit, consent Android, Cloudflare Tunnel WSS, dan transport relay. Cloudflare hanya menyediakan jalur transport; tidak memberikan izin Android dan bukan jaminan keamanan aplikasi.

## Calling

WhatsApp Business voice calling hanya memakai Calling API dan webhook Meta bila tersedia untuk akun. Video call internal WAW memakai WebRTC WAW dengan signaling WAW, STUN/TURN, consent kamera/mikrofon, indikator aktif, dan tombol putus darurat. Kedua jalur diberi label channel dan tidak disamakan tanpa aturan akses.

## Deployment

UI dapat dideploy ke Cloudflare Pages. API, database, queue, webhook, TURN, object storage, dan secret manager harus berjalan pada layanan backend yang mendukung proses server. Tidak ada secret nyata di repository.
