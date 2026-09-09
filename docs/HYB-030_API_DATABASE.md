# HYB-030 — Database dan API Gateway

## Scope

Migration `infra/migrations/0001_hyb030_core.sql` menyediakan boundary awal untuk tenant, user, workspace, membership, conversation, message, media, audit, dan WhatsApp Business webhook event. Semua tabel data tenant membawa `tenant_id`; resource workspace membawa pasangan `workspace_id` dan `tenant_id` untuk mencegah referensi lintas tenant.

Migration menargetkan PostgreSQL dan harus dijalankan melalui migration runner pada environment terkontrol. Production wajib memiliki backup sebelum migration, smoke test setelah migration, serta prosedur rollback atau forward-fix yang terdokumentasi.

## API gateway contract

API gateway wajib membuat request context dari session server-side sebelum memanggil repository. Endpoint minimum yang akan dibangun pada tahap implementasi berikutnya adalah:

| Endpoint | Tujuan | Boundary |
|---|---|---|
| `GET /health` | Liveness | Tidak membaca data tenant |
| `GET /ready` | Readiness database/queue | Tidak membocorkan secret |
| `GET /v1/workspaces` | Daftar workspace | Tenant guard + membership |
| `GET /v1/conversations` | Daftar thread | Tenant/workspace/channel guard |
| `POST /v1/messages` | Membuat pesan internal | `waw_internal` saja pada endpoint ini |
| `POST /v1/webhooks/meta` | Menerima webhook Meta | Signature verification + idempotency |
| `GET /v1/audit` | Membaca audit | Scope `audit:read` |

Semua response error menggunakan envelope yang memiliki `code`, `message`, `requestId`, dan `details` yang sudah direda​ct. API tidak boleh mengembalikan access token provider, cookie WhatsApp, QR secret, atau payload credential.

## Transaction dan idempotency

Pembuatan message, audit record, dan webhook processing harus menggunakan transaction boundary yang jelas. Unique key webhook adalah `(channel, external_id, event_type)` dan menjadi dasar deduplication. Kegagalan berulang masuk ke dead-letter flow tanpa menghapus metadata audit yang dibutuhkan.

## Acceptance criteria HYB-030

Migration fresh install, migration rerun, constraint tenant/workspace, index check, dan rollback/forward-fix harus diuji. API contract test harus membuktikan health/readiness, pagination, error envelope, cross-tenant denial, optimistic concurrency, dan webhook idempotency. Tidak ada secret nyata dalam migration, fixture, log, atau artifact.
