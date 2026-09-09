# HYB-010 — Contracts dan Data Boundary

Package ini adalah kontrak lintas layanan untuk WAW Hybrid. Kontrak bersifat language-neutral agar web, API, webhook worker, realtime service, media service, audit service, dan Android memakai model yang sama.

## Channel wajib

- `waw_internal`: data dan event milik backend WAW.
- `whatsapp_business`: data bisnis yang berasal dari Meta WhatsApp Business Cloud API.

WhatsApp personal WebView/linking tidak termasuk `whatsapp_business` dan tidak boleh dikirim ke API WAW sebagai session, QR, cookie, token, atau chat personal.

## Aturan event

Setiap event wajib memiliki:

- `id`: UUID event internal.
- `schemaVersion`: versi kontrak.
- `tenantId`: UUID tenant pemilik data.
- `workspaceId`: UUID workspace, bila event berada di workspace.
- `channel`: channel data.
- `type`: nama event versioned.
- `externalId`: ID provider/source untuk idempotency; boleh `null` untuk event internal tanpa provider.
- `occurredAt`: timestamp ISO-8601 UTC.
- `actorId`: user/service yang menghasilkan event, bila diketahui.
- `traceId`: ID tracing request/job.
- `payload`: isi event yang sudah divalidasi dan disaring.

Webhook idempotency menggunakan kombinasi `channel`, `externalId`, dan `type`. Payload dead-letter harus disimpan dengan redaction policy dan tidak boleh memuat access token, cookie, QR secret, password, atau data sensitif yang tidak diperlukan.

## Aturan data

Semua entity bisnis harus membawa tenant/workspace boundary, retention policy, audit classification, dan deletion behavior. Provider ID tidak boleh digunakan sebagai pengganti ID internal.

## Validasi

Schema berada di `schemas/`. Contoh valid berada di `examples/`. CI berikutnya wajib menjalankan JSON parse dan schema validation sebelum kontrak dikunci.
