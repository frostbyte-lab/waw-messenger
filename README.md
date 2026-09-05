# WAW Messenger

WAW adalah aplikasi Android yang sedang dikembangkan dengan dua bagian utama:

1. **WhatsApp linked / companion-device path** — mengikuti mekanisme linking resmi yang tersedia untuk perangkat yang didukung.
2. **WAW Workspace** — toolbox produktivitas, dokumen, scanner, keamanan, network diagnostics, dan remote-device management yang merupakan fitur milik WAW sendiri.

> **Status proyek:** Foundation / feasibility & implementation in progress
>
> WAW bukan aplikasi messenger mandiri dengan akun WAW sebagai pengganti akun WhatsApp. Fitur Workspace adalah layanan milik WAW dan tidak boleh digunakan untuk mengambil alih otoritas akun WhatsApp.

## Arsitektur Produk

```text
WAW
│
├── WhatsApp Linked / Companion Device
│   ├── Identity
│   ├── Contacts
│   ├── Chats
│   ├── Messages & sync
│   ├── Media
│   ├── Notifications
│   ├── Calls (if officially supported)
│   └── Device management
│
└── WAW Workspace
    ├── Remote PC / Android
    ├── Document Editor
    ├── PDF Tools
    ├── Camera Scanner
    ├── Image → PDF
    ├── Custom Watermark
    ├── File Manager
    ├── Notes & Tasks
    ├── Network / IP Information
    ├── WAW Shield (Anti-Judol / Anti-Phishing)
    ├── Secure Vault
    ├── Fingerprint / Biometric Lock
    ├── Backup & Sync
    ├── Clipboard Manager
    └── Universal Search
```

WhatsApp secara resmi mendokumentasikan linked devices, termasuk companion phones, serta linking melalui perangkat utama. Kemampuan yang tersedia untuk WAW harus tetap diverifikasi terhadap mekanisme resmi dan kategori perangkat target. citeturn0search0turn0search5

## Prinsip Utama

1. **Official-first** — mekanisme WhatsApp hanya melalui jalur resmi/berwenang yang memang tersedia.
2. **Tidak meminta password WhatsApp.**
3. **Tidak mengambil token, cookie, private key, session secret, atau credential internal WhatsApp.**
4. **Tidak bypass, spoof, replay, atau melemahkan kontrol keamanan.**
5. **Blocked berarti blocked** — jika kemampuan tidak tersedia secara resmi, gunakan status `BLOCKED / NOT_SUPPORTED`.
6. **Workspace adalah WAW-owned** — backend dan data Workspace tidak boleh menjadi pengganti backend WhatsApp.
7. **Privacy & security first** — permission dan data collection harus seminimal mungkin.
8. **Remote access wajib explicit** — perangkat harus dipasangkan dan diotorisasi oleh pemiliknya.
9. **IP geolocation hanya perkiraan** — tidak boleh dipresentasikan sebagai lokasi GPS seseorang.
10. **One feature at a time** — implement → build → test → fix → retest → pass → lock → next.

## Roadmap Utama

Urutan besar proyek mengikuti `ROADMAP.md`.

### Track A — WhatsApp Linked / Companion Device

1. Audit Android foundation
2. Audit feasibility official linking
3. Link Device
4. Account / Identity Sync
5. Contacts
6. Chat Core
7. Message History & Sync
8. Chat Features
9. Media
10. Voice Notes
11. Groups
12. Notifications
13. Calls jika resmi didukung
14. Status / Updates jika resmi didukung
15. Linked Device Management
16. Local Security
17. Network Reliability
18. Full Test Matrix
19. Production Android

**Catatan:** Track A tidak boleh menggunakan protokol privat WhatsApp sebagai shortcut.

### Track B — WAW Workspace

Workspace adalah modul WAW-owned yang dapat dikerjakan tanpa menunggu seluruh fitur WhatsApp selesai, selama tidak bergantung pada kemampuan WhatsApp yang tidak resmi.

Urutan pengerjaan Workspace:

1. **W0 — Workspace Foundation**
2. **W1 — File Manager**
3. **W2 — Document + PDF Core**
4. **W3 — Camera Scanner + Image → PDF**
5. **W4 — Custom Watermark**
6. **W5 — Fingerprint / Biometric + Secure Vault**
7. **W6 — Network / IP Diagnostics**
8. **W7 — WAW Shield / Anti-Judol / Anti-Phishing**
9. **W8 — Notes + Tasks**
10. **W9 — Backup / Sync**
11. **W10 — Remote PC / Android**
12. **W11 — Universal Search**
13. **W12 — Workspace Final Integration**

Detail checklist dan status Workspace berada di `WORKSPACE_PROGRESS.md`.

## Workspace Feature Scope

### Remote PC / Android

- Device pairing
- Explicit authorization
- Screen viewing
- Mouse / keyboard / touch control
- File transfer
- Connection status
- Disconnect
- Revoke access

Remote control tidak boleh berjalan diam-diam atau tanpa otorisasi pemilik perangkat.

### Document & PDF

- TXT editor
- Markdown editor
- PDF viewer
- PDF annotation
- Merge PDF
- Split PDF
- Rotate/reorder pages
- Extract pages
- Compress PDF
- PDF → image
- Image → PDF
- Export/share

### Camera Scanner

- Camera capture
- Edge detection
- Auto crop
- Perspective correction
- Image enhancement
- Multi-page scan
- Page reorder
- Image → PDF
- Preview/export

### Custom Watermark

- Text watermark
- Logo/image watermark
- Position
- Size
- Opacity
- Rotation
- Color
- Repeat/tile
- Per-page configuration
- Presets
- Apply to PDF/image export

### Fingerprint / Biometric

- Workspace lock
- Secure Vault lock
- Document lock
- Auto-lock
- Background lock
- Device credential fallback where appropriate

WAW menggunakan sistem biometric Android; WAW tidak menyimpan data sidik jari pengguna. Android menyediakan `BiometricPrompt` untuk autentikasi biometrik sistem. citeturn0search2

### File Manager

- Folder
- Rename
- Copy/move
- Delete
- Share
- Sort/filter
- Favorites
- Recent files
- Storage usage

### Network / IP Information

Fitur ini adalah **network diagnostics**, bukan alat pelacakan rahasia.

- Public IP
- Local IP
- IPv4/IPv6
- ISP/ASN bila tersedia
- Country
- Region
- City estimate bila tersedia
- Timezone
- Approximate map
- DNS test
- Ping/latency
- Connection diagnostics
- Speed test

IP geolocation bersifat perkiraan. Lokasi presisi perangkat Android membutuhkan mekanisme location dan permission yang sesuai.

### WAW Shield — Anti-Judol / Anti-Phishing

- Domain reputation
- Judol/gambling blocklist
- Custom blocklist
- Phishing detection
- Suspicious redirect detection
- Warning page
- Block / Allow
- Block history
- Report domain
- Privacy-preserving reputation lookup

Sistem harus membedakan antara **terdeteksi**, **mencurigakan**, dan **tidak diketahui**. Jangan menyatakan sebuah domain berbahaya jika sistem tidak memiliki dasar klasifikasi yang memadai.

### Secure Vault

- Private files
- Encrypted storage
- Biometric unlock
- Auto-lock
- Secure deletion policy
- Privacy-aware metadata handling

### Notes / Tasks

- Notes
- Checklist
- Tasks
- Attach files
- Search
- Local persistence

### Backup / Sync

- Backup
- Restore
- Export/import
- Version history
- Conflict handling
- Optional WAW-owned sync

### Clipboard Manager

- Clipboard history
- Pin
- Search
- Auto-expiration
- Sensitive-content exclusion

### Universal Search

Search lintas data Workspace yang memang dimiliki WAW:

- Documents
- PDFs
- Notes
- Tasks
- Files
- Workspace items
- Devices

## Workspace Data Boundary

```text
WAW Workspace
    │
    ├── WAW-owned files/data
    ├── WAW-owned settings
    ├── WAW-owned device pairing
    └── WAW-owned services

WhatsApp account
    │
    └── tetap berada pada mekanisme WhatsApp yang sah
```

Workspace tidak boleh digunakan untuk mengekstrak database, credential, token, cookie, private key, atau session rahasia WhatsApp.

## Repository Structure

```text
waw-messenger/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/waw/messenger/
│       │   ├── MainActivity.kt
│       │   ├── auth/
│       │   └── chat/
│       └── res/
├── backend/
│   ├── worker.js
│   ├── README.md
│   └── migrations/
│       ├── 0001_auth_chat.sql
│       └── 0002_auth_security.sql
├── ROADMAP.md
├── WORKSPACE_PROGRESS.md
└── README.md
```

## Legacy Backend

Direktori `backend/` saat ini masih berisi fondasi WAW chat lama: authentication, session, D1 schema, dan WebSocket.

Kode tersebut adalah **legacy/prototype architecture**, bukan sumber kebenaran akun WhatsApp. Jangan menghapusnya secara massal. Audit dan migrasi harus dilakukan setelah pengganti tervalidasi.

## Testing Policy

Setiap fitur mengikuti:

```text
DESIGN
  ↓
IMPLEMENT
  ↓
BUILD
  ↓
REAL TEST
  ↓
FIX
  ↓
RETEST
  ↓
PASS
  ↓
LOCK
  ↓
NEXT
```

### Definition of Done

Fitur hanya boleh `DONE / LOCKED` jika:

- Implementasi selesai
- Build berhasil
- Main workflow berhasil pada perangkat nyata
- Failure case utama diuji
- Security check lulus
- Tidak ada secret di log
- Dokumentasi diperbarui
- Tidak ada blocker yang diketahui

## Current Status

### WhatsApp Track

| Area | Status |
|---|---|
| Repository foundation | IN PROGRESS |
| Android foundation audit | PENDING |
| Official linking feasibility | PENDING |
| Link Device | NOT STARTED |
| Identity sync | NOT STARTED |
| Contacts | NOT STARTED |
| Chat core | NOT STARTED for target architecture |
| Message sync | NOT STARTED |
| Media | NOT STARTED |
| Voice notes | NOT STARTED |
| Groups | NOT STARTED |
| Notifications | NOT STARTED |
| Calls | NOT STARTED |
| Status / Updates | NOT STARTED |
| Device management | NOT STARTED |
| Security hardening | NOT STARTED |
| Full integration test | NOT STARTED |
| Production release | NOT STARTED |

### Workspace Track

| Stage | Status |
|---|---|
| W0 Foundation | NOT STARTED |
| W1 File Manager | NOT STARTED |
| W2 Document + PDF | NOT STARTED |
| W3 Scanner | NOT STARTED |
| W4 Watermark | NOT STARTED |
| W5 Fingerprint + Vault | NOT STARTED |
| W6 Network/IP | NOT STARTED |
| W7 WAW Shield | NOT STARTED |
| W8 Notes + Tasks | NOT STARTED |
| W9 Backup/Sync | NOT STARTED |
| W10 Remote | NOT STARTED |
| W11 Universal Search | NOT STARTED |
| W12 Final Integration | NOT STARTED |

## Development Rules

1. Jangan redesign besar tanpa alasan teknis.
2. Jangan menghapus file tanpa kebutuhan yang jelas.
3. Fetch file terbaru sebelum melakukan perubahan.
4. Ubah sesedikit mungkin untuk mencapai milestone.
5. Satu milestone pada satu waktu.
6. Test setelah perubahan penting.
7. Lock fitur yang lulus.
8. Jangan lanjut jika milestone sebelumnya masih blocker.
9. README, ROADMAP, dan progress file harus tetap sinkron.
10. Jangan mengklaim `DONE` tanpa hasil pengujian nyata.

## Repository Consolidation

`waw-messenger` adalah repository utama dan sumber kebenaran tunggal untuk implementasi Android, backend, CI/CD, dan dokumentasi WAW. Materi dari repository pendukung telah ditempatkan di folder berikut:

- `docs/CONSOLIDATION_INDEX.md` — indeks dan aturan konsolidasi.
- `docs/product/WAW_ANDROID_PRD.md` — PRD workspace Android.
- `docs/product/WA_WORKSPACE_SPEC.md` — spesifikasi WA.W Workspace.
- `docs/product/WA_WORKSPACE_TODO.md` — TODO workspace.
- `docs/architecture/WAW_ANDROID_ARCHITECTURE.md` — arsitektur awal Android.
- `docs/architecture/WAW_ANDROID_MODULES.md` — daftar modul.
- `docs/ui/WAW_ANDROID_UI_GUIDE.md` — panduan UI.

Setelah Tahap 1, commit fitur dan dokumentasi baru wajib diarahkan ke repository ini. `waw-android` dan `wa-workspace-v4-2-saas-lengkap` belum dihapus atau diubah menjadi arsip agar riwayat dan sumber asli tetap aman sampai konsolidasi diverifikasi.

## References

- `ROADMAP.md` — master roadmap produk dan linked-device architecture.
- `WORKSPACE_PROGRESS.md` — checklist dan progres Workspace.
- `backend/README.md` — dokumentasi backend legacy.

Official WhatsApp Help Center harus diprioritaskan untuk kemampuan linked devices, companion phones, supported devices, dan device management. citeturn0search0turn0search3turn0search5

---

**WAW Messenger — Build carefully. Test for real. Lock what passes.**
