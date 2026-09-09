# WAW Hybrid API Runtime

Service ini adalah titik runtime untuk health/readiness, WAW internal message, audit, dan WhatsApp Business webhook ingress. Semua request tenant-scoped membutuhkan `x-tenant-id` dan `x-workspace-id`; channel personal WhatsApp tidak diterima.

`DATABASE_URL` wajib untuk staging/production. Tanpa database, service hanya dapat dipakai untuk unit test dengan repository memory injection dan tidak boleh dipublikasikan.

Meta app secret dan verify token hanya dibaca dari environment server. Raw webhook payload tidak ditulis ke log.
