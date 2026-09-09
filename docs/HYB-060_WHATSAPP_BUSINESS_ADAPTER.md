# HYB-060 — WhatsApp Business Cloud Adapter

## Scope

HYB-060 menyediakan boundary server-side untuk WhatsApp Business Cloud API resmi Meta. Adapter ini hanya menangani nomor bisnis yang dikonfigurasi pada tenant. Adapter tidak memproses WhatsApp personal, QR login, cookie, session browser, kontak personal, atau credential pada APK.

## Data boundary

Setiap record memiliki `channel: whatsapp_business`, `tenantId`, dan `workspaceId`. Data inbound dipetakan dari `externalId` Meta ke model WAW Business. Data tidak boleh dipindahkan ke channel `waw_internal` tanpa operasi aplikasi yang eksplisit dan teraudit.

## Webhook processing

Endpoint webhook memverifikasi challenge dan signature `X-Hub-Signature-256` menggunakan app secret yang hanya tersedia di server. Payload disimpan dengan idempotency key berupa gabungan tenant, event type, dan external ID. Event duplikat tidak boleh menghasilkan pesan atau delivery status kedua.

Event yang gagal diproses mengikuti bounded retry dengan backoff. Setelah batas percobaan tercapai, payload masuk dead-letter queue dengan audit record dan error code. Raw payload tidak boleh masuk log aplikasi biasa.

## Outbound operations

Pengiriman text, template, dan media dilakukan oleh adapter melalui Graph API menggunakan access token server-side. Client hanya menerima hasil normalisasi dan delivery status. Token Meta tidak pernah dikirim ke client, URL publik, log, atau exception response.

## Retention and deletion

Tenant harus memiliki retention policy untuk pesan dan media bisnis. Deletion menandai record dan menghapus object media sesuai policy. Export, deletion, perubahan konfigurasi nomor bisnis, dan dead-letter replay wajib diaudit.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Channel | Semua record adapter memiliki `whatsapp_business`; tidak ada personal WhatsApp flow |
| Secrets | Token/app secret hanya server-side dan tidak tampil di log atau APK |
| Inbound | Signature valid diterima; signature invalid ditolak |
| Idempotency | Event sama tidak menggandakan message/status |
| Retry | Error transient di-retry dengan batas dan backoff |
| DLQ | Error permanen atau exhausted retry masuk dead-letter dengan audit |
| Outbound | Text/template/media dinormalisasi tanpa membocorkan token Meta |
| Retention | Expiry dan deletion bisnis dapat dijalankan serta diaudit |
