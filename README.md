# WAW Messenger

WAW adalah aplikasi Android yang sedang dikembangkan sebagai **linked / companion device** untuk akun WhatsApp yang sah milik pengguna.

> **Status proyek:** Foundation / feasibility & implementation in progress
>
> WAW bukan aplikasi messenger mandiri dengan akun WAW sendiri. Target utamanya adalah pengalaman Android yang terhubung ke akun WhatsApp melalui mekanisme linking yang resmi dan didukung.

## Tujuan

Target WAW adalah menyediakan aplikasi Android yang dapat menjadi perangkat pendamping untuk akun WhatsApp pengguna, dengan tetap mempertahankan model keamanan dan otorisasi resmi.

Arsitektur target:

```text
WhatsApp Primary Phone
        │
        │ Official device linking
        ▼
WAW Android
        │
        ├── WhatsApp account/session
        ├── Identity & profile
        ├── Contacts
        ├── Chat list
        ├── Messages & sync
        ├── Media
        ├── Notifications
        ├── Calls
        └── Device management
```

WhatsApp secara resmi mendukung perangkat tertaut, termasuk companion phones, dan memungkinkan beberapa perangkat tertaut pada satu akun. citeturn0search2turn0search3

## Prinsip Utama

1. **Official-first** — hanya menggunakan mekanisme yang resmi, terdokumentasi, atau memang tersedia untuk integrasi yang sah.
2. **Tidak meminta password WhatsApp** — WAW tidak boleh meminta atau menyimpan password akun WhatsApp pengguna.
3. **Tidak mengambil token/cookie/private key** — kredensial internal, token sesi, cookie, atau material kunci privat WhatsApp tidak boleh diekstrak, disalin, atau direplay.
4. **Tidak bypass security** — tidak melakukan bypass terhadap verifikasi, pairing, proteksi, atau mekanisme keamanan WhatsApp.
5. **Tidak menganggap API internal sebagai API publik** — endpoint/protokol internal yang ditemukan melalui reverse engineering tidak otomatis dianggap sebagai API yang boleh digunakan.
6. **Blocked berarti blocked** — jika sebuah kemampuan tidak tersedia melalui mekanisme resmi yang dapat digunakan WAW, tandai `BLOCKED / NOT_SUPPORTED`; jangan membuat implementasi palsu yang menyamarkan keterbatasan tersebut.
7. **Privacy & security first** — data pengguna harus diproses seminimal mungkin dan diamankan di perangkat.
8. **One feature at a time** — satu fitur harus selesai, diuji, diperbaiki, diuji ulang, lalu di-lock sebelum berpindah ke fitur berikutnya.

## Mekanisme Linking

WhatsApp mendukung linking perangkat dengan QR code pada perangkat yang didukung. Pada alur resmi, perangkat utama mengonfirmasi proses linking dan melakukan pemindaian QR. citeturn0search1

WhatsApp juga mendokumentasikan companion phones sebagai salah satu jenis linked device. citeturn0search2

**Catatan penting untuk implementasi WAW:** dukungan companion phone pada aplikasi WhatsApp resmi tidak berarti tersedia SDK/API publik untuk membuat aplikasi Android pihak ketiga yang dapat berbicara langsung dengan protokol internal WhatsApp. Karena itu, tahap linking WAW harus diawali dengan audit kelayakan mekanisme resmi yang benar-benar dapat digunakan aplikasi ini.

## Scope Produk

### In scope

- Android application foundation
- Official/authorized device-linking path, jika tersedia
- Account/identity sync yang tersedia melalui mekanisme resmi
- Contacts
- Chat list
- 1-to-1 chat
- Message history & synchronization
- Media
- Voice notes
- Groups
- Reply / forward / delete / reaction / search sesuai dukungan resmi
- Notifications
- Voice calls dan video calls jika mekanisme resmi memungkinkan
- Status / Updates jika didukung
- Linked-device management
- Local security
- Network reliability
- Testing dan production hardening

### Out of scope

- Membuat akun WhatsApp melalui backend WAW
- Menjadikan username/password WAW sebagai identitas utama produk
- Mengambil atau menyimpan password WhatsApp
- Mengambil token/cookie/private key WhatsApp
- Meniru atau replay protokol privat tanpa otorisasi
- Membypass pairing, verification, rate limit, atau security control
- Mengklaim fitur WhatsApp tersedia jika belum lolos pengujian nyata

## Roadmap

Urutan kerja mengikuti `ROADMAP.md`.

### Phase 1 — Official Linked / Companion Device

- Audit Android foundation
- Audit feasibility mekanisme linking resmi
- Hentikan WAW username/password sebagai jalur utama
- Rancang Link Device screen
- Implementasikan mekanisme linking resmi yang memang tersedia
- Uji linking menggunakan akun pengujian milik sendiri
- Lock linking

### Phase 2 — Account / Identity Sync

- Sinkronisasi identitas akun yang tersedia
- Profile
- Display name
- Avatar
- Account state
- Session/device state
- Test identity sync
- Lock identity

### Phase 3 — Contacts

- Contact permission
- Contact discovery/sync
- Mapping contact ke identitas WhatsApp yang tersedia
- Test contact sync
- Lock contacts

### Phase 4 — Chat Core

- Chat list
- Conversation model
- 1-to-1 chat
- Message send/receive
- Local persistence
- Sync state
- Test dua arah
- Lock chat core

### Phase 5 — Message History & Sync

- Initial sync
- Incremental sync
- Reconnect
- Offline queue
- Duplicate protection
- Delivery/read state
- History consistency
- Lock sync

### Phase 6 — Chat Features

- Reply
- Forward
- Delete
- Reactions
- Search
- Message metadata
- Lock feature set

### Phase 7 — Media

- Image
- Video
- Document
- Audio
- Upload/download state
- Local caching
- Lock media

### Phase 8 — Voice Notes

- Recording
- Playback
- Upload/sync
- Local storage policy
- Lock voice notes

### Phase 9 — Groups

- Group list
- Group members
- Group messages
- Group metadata
- Group permissions where supported
- Lock groups

### Phase 10 — Notifications

- Push/event handling
- Message notifications
- Notification actions
- Background handling
- Notification security
- Lock notifications

### Phase 11 — Calls

- Voice call feasibility
- Video call feasibility
- Implement only through supported mechanisms
- Network handling
- Permissions
- Lock calls only after real testing

### Phase 12 — Status / Updates

- View status/updates where supported
- Create/manage status only if officially supported for the target linked device
- Lock status

### Phase 13 — Linked Device Management

- Device information
- Connection state
- Reconnect
- Logout/unlink
- Session lifecycle
- Account safety controls

WhatsApp menyediakan pengelolaan perangkat tertaut dan memungkinkan pengguna mengeluarkan perangkat yang tertaut dari perangkat utama. citeturn0search8

### Phase 14 — Local Security

- Secure local storage
- Session protection
- Android Keystore where appropriate
- Screenshot/privacy policy where appropriate
- Logging minimization
- Data deletion
- Threat-model review

### Phase 15 — Network Reliability

- Retry
- Timeout
- Reconnect
- Offline state
- Background constraints
- Error classification
- Observability tanpa membocorkan data sensitif

### Phase 16 — Full Test Matrix

- Fresh install
- Link
- Unlink
- Reconnect
- Offline/online transition
- Send/receive
- History sync
- Media
- Groups
- Notifications
- Calls if supported
- Multiple linked devices
- Failure recovery
- Security tests

### Phase 17 — Production Android

- Release configuration
- Signing
- Versioning
- Crash monitoring
- Performance profiling
- Battery/network profiling
- Privacy review
- Final regression
- Production release

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
└── README.md
```

## Legacy Backend

Direktori `backend/` saat ini berisi fondasi WAW chat lama yang pernah dibuat, termasuk authentication, session, D1 schema, dan WebSocket.

Kode tersebut **tidak otomatis menjadi arsitektur final WAW**. Setelah target produk berubah menjadi linked/companion device, komponen lama harus diaudit dan hanya dipertahankan jika memang dibutuhkan untuk layanan milik WAW sendiri.

Jangan menghapus komponen legacy secara massal. Setiap penghapusan atau migrasi harus dilakukan setelah audit dan pengujian.

## Backend WAW

Backend WAW hanya boleh digunakan untuk layanan yang memang dimiliki dan dikendalikan WAW, misalnya:

- konfigurasi aplikasi
- telemetry yang benar-benar diperlukan dan telah dianonimkan/minimalkan
- crash/error reporting yang aman
- layanan WAW sendiri
- fitur tambahan yang tidak mengambil alih otoritas akun WhatsApp

Backend WAW **bukan** sumber otoritas untuk akun WhatsApp pengguna.

## Android Architecture Direction

Arsitektur Android akan dipisahkan secara bertahap menjadi lapisan yang jelas:

```text
UI
 │
 ▼
Presentation / ViewModel
 │
 ▼
Domain
 │
 ├── Identity
 ├── Contacts
 ├── Conversations
 ├── Messages
 ├── Media
 ├── Notifications
 └── Device / Session
 │
 ▼
Data
 │
 ├── Official/authorized integration boundary
 ├── Local database
 ├── Secure storage
 └── WAW-owned backend (only where needed)
```

Implementasi aktual dapat berkembang selama tidak melanggar prinsip pada dokumen ini dan tetap sinkron dengan `ROADMAP.md`.

## Data & Security Rules

### Jangan pernah

- meminta password WhatsApp pengguna
- menyimpan password WhatsApp
- meminta OTP WhatsApp untuk diproses oleh backend WAW
- mengekstrak token sesi internal
- mengekstrak cookie sesi internal
- mengekstrak private key
- menyalin database internal WhatsApp secara tidak sah
- replay kredensial atau token internal
- bypass mekanisme keamanan
- menyimpan data chat lebih lama dari yang diperlukan
- mengirim isi chat ke server WAW tanpa kebutuhan dan dasar yang jelas

### Wajib

- gunakan secure storage untuk secret yang memang diperlukan
- minimalkan logging
- jangan mencetak token/session ke log
- validasi semua input
- batasi permission Android
- encrypt data sensitif saat diperlukan
- sediakan mekanisme logout/unlink yang jelas
- hapus data lokal yang tidak lagi diperlukan

WhatsApp menyatakan bahwa pesan, media, dan panggilan pada linked devices tetap menggunakan end-to-end encryption, dan setiap perangkat tertaut terhubung secara independen. citeturn0search2

## Testing Policy

Tidak boleh menandai fitur sebagai selesai hanya karena kode berhasil dikompilasi.

Setiap milestone harus melalui:

```text
Implement
   ↓
Build
   ↓
Run
   ↓
Real test
   ↓
Find bugs
   ↓
Fix
   ↓
Retest
   ↓
PASS
   ↓
LOCK
```

### Definition of Done

Sebuah fitur hanya boleh diberi status **DONE / LOCKED** apabila:

- implementasi selesai
- build berhasil
- tidak ada error blocker
- skenario utama berhasil
- failure case utama diuji
- data tidak bocor ke log
- security review dasar selesai
- hasil pengujian dicatat
- tidak ada dependency tersembunyi yang belum diketahui

## Current Status

| Area | Status |
|---|---|
| Repository foundation | In progress |
| Android foundation audit | Pending |
| Official linking feasibility audit | Pending |
| Link Device | Not started |
| Identity sync | Not started |
| Contacts | Not started |
| Chat core | Not started for target architecture |
| Message sync | Not started for target architecture |
| Media | Not started |
| Voice notes | Not started |
| Groups | Not started |
| Notifications | Not started |
| Calls | Not started |
| Status / Updates | Not started |
| Device management | Not started |
| Security hardening | Not started |
| Full integration test | Not started |
| Production release | Not started |

## Important Current Limitation

Pada saat README ini dibuat, belum boleh diasumsikan bahwa WAW dapat langsung menjadi aplikasi companion WhatsApp pihak ketiga hanya dengan membuat QR scanner atau meniru protokol internal WhatsApp.

Dokumentasi resmi WhatsApp memang menunjukkan dukungan companion phones, tetapi alur resmi tersebut berada dalam ekosistem aplikasi/perangkat yang didukung WhatsApp. citeturn0search2turn0search7

Karena itu **Phase 1 dimulai dari feasibility audit**, bukan dari implementasi protokol privat.

Jika mekanisme resmi yang dapat digunakan aplikasi WAW tidak tersedia, status fitur harus menjadi:

```text
BLOCKED / NOT_SUPPORTED
```

bukan dipalsukan sebagai `DONE`.

## Development Workflow

Aturan kerja proyek:

1. Jangan melakukan redesign besar tanpa alasan teknis.
2. Jangan menghapus file tanpa kebutuhan yang jelas.
3. Fetch kondisi file terbaru sebelum mengubah file.
4. Ubah sesedikit mungkin untuk mencapai milestone.
5. Satu milestone pada satu waktu.
6. Test setelah setiap perubahan penting.
7. Lock fitur yang sudah lulus.
8. Jangan lanjut ke fitur berikutnya jika fitur sebelumnya masih memiliki blocker.
9. Dokumentasi harus tetap sinkron dengan `ROADMAP.md`.
10. Semua klaim `DONE` harus didukung hasil pengujian nyata.

## Reference

- Roadmap proyek: `ROADMAP.md`
- Backend legacy: `backend/README.md`
- Migration: `backend/migrations/0001_auth_chat.sql`
- Security migration: `backend/migrations/0002_auth_security.sql`

## Official WhatsApp References

Dokumentasi resmi yang menjadi referensi untuk feasibility linked-device:

- WhatsApp Help Center — About linked devices
- WhatsApp Help Center — How to link a device
- WhatsApp Help Center — How to link a device using phone number
- WhatsApp Help Center — Companion phone / WhatsApp Business linking

Referensi resmi harus diprioritaskan dibanding dokumentasi pihak ketiga ketika menentukan kemampuan linking, supported devices, dan security behavior.

---

**WAW Messenger — Build carefully. Test for real. Lock what passes.**
