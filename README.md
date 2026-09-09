# WAW Business Workspace

## Sumber kebenaran sistem

Dokumen ini menggantikan catatan sistem lama. Arsitektur aktif WAW adalah **WAW Hybrid**: satu UI WAW dengan batas komunikasi yang terpisah antara `waw_internal` dan `whatsapp_business`.

Catatan remote standalone lama, mockup login WhatsApp, credential buatan WAW, dan asumsi bahwa relay WAW mengelola sesi WhatsApp personal dinyatakan **legacy** dan tidak boleh dijadikan dasar implementasi baru.

## Produk aktif

WAW menyediakan shell produk dengan branding, navigasi, Workspace, consent, dan fitur perangkat milik WAW. Untuk komunikasi WhatsApp, linking, chat, media, panggilan, Status, cookie, QR, dan session, WAW menggunakan komponen resmi WhatsApp Web pada `https://web.whatsapp.com`.

WAW tidak meminta password WhatsApp dan tidak mengirim cookie, QR token, session key, credential, atau isi chat WhatsApp ke backend WAW.

## Dua channel sistem

| Channel | Fungsi | Sumber data |
|---|---|---|
| `waw_internal` | Akun, tenant, workspace, chat internal, Status WAW, WebRTC, assignment, notifikasi, audit, dan Remote Workspace | Backend WAW |
| `whatsapp_business` | Pesan bisnis, media bisnis, template, delivery status, webhook, dan Calling API jika memenuhi syarat | Meta WhatsApp Business Cloud API |

Data kedua channel tidak boleh dicampur tanpa `channel`, `tenantId`, `workspaceId`, `role`, `retention`, dan audit boundary yang jelas.

## Batas integrasi WhatsApp

Implementasi WhatsApp wajib mengikuti jalur resmi. WAW boleh menyediakan header, tab, status wrapper, dan navigasi visual, tetapi fungsi WhatsApp tetap dijalankan oleh WhatsApp Web resmi atau WhatsApp Business Cloud API yang sesuai.

WAW dilarang:

- Membuat login WhatsApp sendiri.
- Meminta atau menyimpan password WhatsApp.
- Mengambil cookie, QR secret, token, atau private key WhatsApp.
- Menyalin UI WhatsApp sebagai backend alternatif.
- Mengirim data chat personal ke relay WAW.
- Memproses Status personal sebagai data terpisah.

Dokumen batas resmi: [docs/official-whatsapp-integration-boundaries.md](docs/official-whatsapp-integration-boundaries.md).

## Workspace dan Remote

Workspace adalah fitur WAW, bukan fitur internal WhatsApp. Remote Workspace tetap boleh memakai Host Companion dan relay WSS, tetapi harus memiliki explicit consent, token one-time dan expiry, room isolation, foreground service, MediaProjection, Accessibility, file picker sesuai aturan Android, revoke, emergency disconnect, dan audit lifecycle session.

Remote standalone lama hanya dipertahankan sebagai komponen migrasi/legacy sampai seluruh kapabilitasnya berada di dalam shell WAW Hybrid.

## Urutan implementasi resmi

Setiap milestone wajib mengikuti:

```text
DESIGN → IMPLEMENT → BUILD → TEST → SECURITY REVIEW → DOCUMENT → LOCK
```

| Prefix | Milestone |
|---|---|
| `HYB-000` | Baseline, CI, secret scan, branch protection |
| `HYB-010` | Contracts dan channel schema |
| `HYB-020` | Auth, tenant guard, role, audit |
| `HYB-030` | Database dan API gateway |
| `HYB-040` | UI shell dan design system parity |
| `HYB-050` | WAW internal chat, Status, dan realtime |
| `HYB-060` | WhatsApp Business adapter dan webhook |
| `HYB-070` | Media pipeline |
| `HYB-080` | WebRTC dan calling eligibility |
| `HYB-090` | Workspace integration |
| `HYB-100` | Workspace Remote dan Cloudflare Tunnel |
| `HYB-110` | Security, observability, dan release |

Urutan lengkap: [IMPLEMENTATION_ORDER.md](https://github.com/frostbyte-lab/waw-messenger/blob/docs/wa-hybrid-implementation-order/docs/IMPLEMENTATION_ORDER.md).

## Status saat ini

Branch `main` memprioritaskan WAW UI shell dan WhatsApp Web resmi. Baseline security hybrid dan master architecture tersedia pada branch hybrid khusus, tetapi belum seluruh backend hybrid production digabungkan ke `main`.

Yang sudah aktif di `main`:

- WAW UI shell.
- Linking langsung ke WhatsApp Web resmi.
- WebView Android yang membatasi origin ke `web.whatsapp.com`.
- Permission kamera/mikrofon hanya untuk origin resmi.
- Workspace WAW dan modul perangkat yang tersedia.
- CI Android dan release artifact.

Yang masih menjadi pekerjaan hybrid:

- Shared contracts package.
- Auth, tenant guard, role, dan audit backend.
- API gateway dan database production.
- WhatsApp Business Cloud API dan webhook Meta.
- Media service, queue, dan retention.
- Realtime/WebRTC production.
- Migrasi penuh Remote Workspace ke shell WAW.
- Observability, staging, rollback, dan security review production.

## Visual contract

Web dan APK harus memiliki visual parity dengan referensi WAW. Perubahan UI wajib mempertahankan branding, shell navigasi, warna, typography, spacing, card, status indicator, button treatment, responsive behavior, dan animasi utama. Perbedaan hanya diperbolehkan bila dipaksa oleh platform, seperti dialog permission Android, MediaProjection, Accessibility settings, file picker, dan safe-area native.

Referensi: [docs/UI_AUDIT_WAW.md](docs/UI_AUDIT_WAW.md).

## Aturan kontribusi

Setiap perubahan sistem harus mencantumkan scope, out of scope, dependency, acceptance criteria, test evidence, security notes, dan rollback/cleanup. Jangan menghidupkan kembali catatan atau alur legacy tanpa keputusan arsitektur baru yang terdokumentasi.
