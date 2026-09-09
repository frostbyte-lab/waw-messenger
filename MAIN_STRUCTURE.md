# WAW Hybrid — Struktur Utama

## Aturan utama

WAW memiliki satu UI produk yang menjadi shell utama. Komunikasi dibagi secara tegas menjadi dua channel:

- `waw_internal`: akun, tenant, workspace, chat internal, Status WAW, WebRTC, assignment, notifikasi, audit, dan Remote Workspace.
- `whatsapp_business`: WhatsApp Business Cloud API resmi Meta untuk pesan bisnis, media, template, delivery status, webhook, dan Calling API bila memenuhi syarat.

Catatan remote standalone lama, login WhatsApp buatan WAW, dan credential WhatsApp buatan sendiri adalah **legacy**. Catatan tersebut tidak boleh menjadi dasar perubahan baru.

## WhatsApp resmi

Untuk linking dan komunikasi WhatsApp resmi, WAW membuka:

```text
https://web.whatsapp.com
```

WhatsApp Web menangani login, QR/linking, chat, sinkronisasi, media, panggilan, Status, cookie, dan session. WAW hanya menyediakan shell visual dan navigasi yang aman.

WAW tidak boleh:

- Meminta password WhatsApp.
- Mengambil cookie, QR secret, token, session key, atau private key.
- Mengirim isi chat personal ke backend WAW.
- Membuat backend alternatif untuk WhatsApp personal.
- Menyimulasikan Status atau daftar kontak WhatsApp sebagai data terpisah.

## Shell WAW

Shell WAW mempertahankan:

- Logo dan branding WAW.
- Header WAW Business.
- Tab Chat, Panggilan, Status, Fitur, dan Workspace.
- Status koneksi resmi.
- Area konten WhatsApp Web resmi.
- Workspace milik WAW.

Workspace tidak boleh mengklaim fitur internal WhatsApp.

## Modul Workspace

Modul Workspace berada di bawah shell WAW:

1. **Remote Workspace** — consent, OTP/invite, screen sharing, relay WSS, input, transfer file, revoke, dan audit.
2. **IP & Location Tracker** — hanya dengan izin Android yang sesuai.
3. **Scan PDF & Document Tools** — file manager, scan, edit, dan export.
4. **Fingerprint Attendance** — biometric gate dan absensi lokal.
5. **Custom Watermark** — logo, timestamp, lokasi, kompas, dan export.

Setiap kartu harus membuka fitur nyata atau menampilkan status `NOT_AVAILABLE`. Tidak boleh ada data demo atau tombol mati.

## Remote Workspace

Remote Workspace adalah fitur WAW, bukan fitur WhatsApp. Sesi wajib memiliki:

- Explicit consent User.
- Token one-time dan expiry.
- Room isolation.
- MediaProjection consent.
- Accessibility consent.
- Foreground notification.
- File picker eksplisit.
- Revoke dan emergency disconnect.
- Audit lifecycle.
- Tidak ada token Cloudflare di APK.

## Struktur target hybrid

```text
apps/web                 # UI WAW dan responsive shell
apps/api                 # auth, tenant guard, workspace, webhook, health
packages/contracts       # DTO, channel enum, event schema, validation
packages/ui              # design system WAW
services/whatsapp        # Meta Graph API adapter dan webhook
services/realtime        # WebSocket, presence, receipts, WebRTC signaling
services/media           # upload, scan, thumbnail, retention, signed URL
docs                     # architecture, security, decisions, runbooks
infra                    # migration, deployment, monitoring, backup
```

## Urutan pengerjaan

```text
HYB-000 → HYB-010 → HYB-020 → HYB-030 → HYB-040 → HYB-050
→ HYB-060 → HYB-070 → HYB-080 → HYB-090 → HYB-100 → HYB-110
```

Urutan resmi mengikuti:

```text
DESIGN → IMPLEMENT → BUILD → TEST → SECURITY REVIEW → DOCUMENT → LOCK
```

Lihat [README.md](README.md) dan [IMPLEMENTATION_ORDER.md](https://github.com/frostbyte-lab/waw-messenger/blob/docs/wa-hybrid-implementation-order/docs/IMPLEMENTATION_ORDER.md) sebagai sumber kebenaran sistem.

## Aturan visual

Web dan APK wajib memiliki visual parity terhadap referensi WAW: branding, shell, warna, typography, spacing, card, status, button treatment, responsive behavior, dan animasi. Perbedaan hanya diperbolehkan untuk dialog dan layar yang dipaksa oleh platform native.
