# HYB-020 — Auth, Tenant, Role, dan Audit

## Keputusan boundary

Request WAW harus menghasilkan request context dari session yang sudah diverifikasi server. `tenantId` dan `workspaceId` dari body atau query tidak boleh dipercaya sebagai sumber authorization. Server mengambil membership dan role dari database berdasarkan subject/session, lalu menerapkan tenant guard sebelum repository atau service dipanggil.

Password/OAuth, MFA, refresh token, recovery, dan logout hanya berlaku untuk akun WAW. Tidak ada credential WhatsApp personal dalam auth service WAW.

## Role

Role minimum adalah `owner`, `admin`, `operator`, `member`, dan `viewer`. Role menentukan kemampuan, sedangkan tenant dan workspace menentukan ruang data. Scope harus berbentuk `resource:action`, misalnya `workspace:read`, `remote_session:revoke`, atau `audit:read`.

## Cross-tenant rule

Setiap query resource wajib memuat tenant predicate dari request context. Request yang mencoba mengakses resource tenant lain harus menghasilkan `403` atau response yang tidak membocorkan keberadaan resource, sesuai endpoint. Membership workspace juga harus diverifikasi secara terpisah dari membership tenant.

## Audit

Audit wajib dibuat untuk perubahan role, membership, assignment, export, webhook processing, remote access, file sharing, revoke, dan tindakan sensitif. Record hanya menyimpan metadata minimum yang diperlukan: actor, tenant, workspace, action, resource, result, reason, dan trace ID. Password, token, cookie, QR secret, private key, dan isi pesan sensitif tidak boleh dicatat.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Session | Request tanpa session valid ditolak |
| Tenant | `tenantId` dari client tidak dapat menggantikan tenant session |
| Workspace | Membership workspace diverifikasi sebelum akses |
| Role | Scope yang tidak dimiliki menghasilkan `403` |
| Audit | Allowed, denied, dan failed actions memiliki trace ID |
| Privacy | Audit/log tidak memuat credential atau session WhatsApp |
| Rate limit | Login, recovery, webhook, dan remote actions memiliki pembatasan |
