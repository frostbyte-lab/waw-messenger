# WAW — MASTER ROADMAP PEMBANGUNAN

## ATURAN UTAMA

> **SATU FITUR → SELESAI → TEST → FIX → LOCK → BARU LANJUT**

WAW adalah aplikasi messenger nyata. Jangan menganggap fitur selesai hanya karena UI sudah tampil. Setiap tahap harus mempunyai implementasi, backend/data bila diperlukan, pengujian, perbaikan, dan status LOCK sebelum tahap berikutnya dimulai.

### Status
- `[ ]` Belum dikerjakan
- `[~]` Sedang dikerjakan
- `[✓]` Selesai dan sudah dites
- `[LOCK]` Selesai, dites, dan fondasinya dikunci

---

# PHASE 0 — FONDASI PROJECT

## Repository
- [✓] Repository resmi: `frostbyte-lab/waw-messenger`
- [ ] Struktur project final
- [ ] README utama
- [ ] Dokumentasi arsitektur
- [ ] Aturan perubahan project

## Android
- [✓] Kotlin
- [✓] Jetpack Compose
- [ ] Build configuration stabil
- [ ] AndroidManifest
- [ ] Permission dasar
- [ ] Debug/release configuration

## Backend
- [✓] Cloudflare Worker dasar
- [ ] Wrangler configuration
- [ ] Cloudflare D1
- [ ] Environment development
- [ ] Environment production
- [ ] API routing

### CHECKPOINT
- [ ] Android build berhasil
- [ ] Worker berjalan
- [ ] D1 dapat diakses
- [ ] Tidak ada error fundamental

**STATUS: LOCK**

---

# PHASE 1 — AUTHENTICATION

## Database
- [ ] `users`
- [ ] `sessions`
- [ ] Password hash
- [ ] Created/updated timestamps

## Registration
- [ ] Username
- [ ] Email
- [ ] Password
- [ ] Validasi input
- [ ] Cek duplikat
- [ ] Simpan user

## Login
- [ ] Login username/email
- [ ] Verifikasi password
- [ ] Session/token
- [ ] Expiration
- [ ] Logout

## Android
- [ ] Login screen
- [ ] Register screen
- [ ] Auth repository
- [ ] Session storage
- [ ] Auto-login
- [ ] Logout
- [ ] Auth state

### TEST
- [ ] User A register
- [ ] User B register
- [ ] A login
- [ ] B login
- [ ] Logout
- [ ] Login ulang
- [ ] Token invalid ditolak

**STATUS: LOCK**

---

# PHASE 2 — USER PROFILE

- [ ] Profile API
- [ ] Get profile
- [ ] Update nama
- [ ] Update username
- [ ] Update avatar
- [ ] Online/offline
- [ ] Last seen
- [ ] Profile screen Android

### TEST
- [ ] A melihat profile A
- [ ] B melihat profile A
- [ ] Update profile
- [ ] Data tetap setelah logout/login

**STATUS: LOCK**

---

# PHASE 3 — CONTACTS

- [ ] User search
- [ ] Contact relationship
- [ ] Add contact
- [ ] Remove contact
- [ ] List contacts
- [ ] Contact screen
- [ ] Search UI

### TEST
- [ ] A mencari B
- [ ] A menambahkan B
- [ ] B tampil sebagai contact
- [ ] Remove berhasil

**STATUS: LOCK**

---

# PHASE 4 — 1-TO-1 CHAT

## Database
- [ ] `conversations`
- [ ] `conversation_members`
- [ ] `messages`

## Message
- [ ] ID
- [ ] Conversation ID
- [ ] Sender ID
- [ ] Text
- [ ] Created time
- [ ] Status
- [ ] Deleted state

## Backend
- [ ] Create conversation
- [ ] List conversations
- [ ] Get messages
- [ ] Send message
- [ ] Conversation authorization
- [ ] Message pagination/history

## Android
- [ ] Conversation list
- [ ] Chat screen
- [ ] Message bubble
- [ ] Input
- [ ] Send
- [ ] Load history
- [ ] Loading state
- [ ] Error state
- [ ] Empty state

### TEST
- [ ] A kirim pesan ke B
- [ ] B menerima
- [ ] Tutup aplikasi
- [ ] Buka kembali
- [ ] History masih ada
- [ ] Login ulang
- [ ] History tetap ada

**STATUS: LOCK**

---

# PHASE 5 — REAL-TIME CHAT

## WebSocket
- [✓] WebSocket dasar
- [ ] Authentication WebSocket
- [ ] User identity binding
- [ ] Conversation authorization
- [ ] Persist sebelum broadcast
- [ ] Broadcast ke anggota conversation
- [ ] Reconnect
- [ ] Connection state
- [ ] Duplicate protection

## Message State

`SENDING → SENT → DELIVERED → READ`

- [ ] Sending state
- [ ] Sent ACK
- [ ] Delivered ACK
- [ ] Read ACK
- [ ] Retry

## Sync
- [ ] Reconnect sync
- [ ] Ambil pesan tertinggal
- [ ] Sinkronisasi message ID/time
- [ ] Hindari duplicate

### TEST WAJIB
- [ ] Device A kirim
- [ ] Device B menerima real-time
- [ ] SENT berhasil
- [ ] DELIVERED berhasil
- [ ] READ berhasil
- [ ] Internet diputus
- [ ] Internet disambungkan
- [ ] Pesan tersinkron
- [ ] Tidak ada duplicate

**STATUS: LOCK**

> Setelah tahap ini lulus, Chat Core dianggap selesai.

---

# PHASE 6 — CHAT CORE FEATURES

Kerjakan satu per satu. Setiap subfitur: **IMPLEMENT → TEST → LOCK**.

## Reply
- [ ] Reply message
- [ ] Preview pesan asli
- [ ] Jump ke pesan asli

## Forward
- [ ] Pilih pesan
- [ ] Pilih tujuan
- [ ] Forward
- [ ] Simpan sebagai pesan baru

## Delete
- [ ] Delete for me
- [ ] Delete for everyone
- [ ] Permission
- [ ] Placeholder pesan dihapus

## Unread
- [ ] Unread counter
- [ ] Mark read
- [ ] Conversation unread
- [ ] Global unread

## Search
- [ ] Search conversation
- [ ] Search message
- [ ] Highlight result

**STATUS: LOCK**

---

# PHASE 7 — LOCAL CACHE & OFFLINE SYNC

- [ ] Room database
- [ ] Local message cache
- [ ] Conversation cache
- [ ] Offline message queue
- [ ] Sync engine
- [ ] Conflict handling
- [ ] Retry strategy

### TEST
- [ ] Airplane mode
- [ ] Buka chat
- [ ] Pesan lama tetap terlihat
- [ ] Kirim saat offline
- [ ] Internet kembali
- [ ] Pesan terkirim
- [ ] Sync berhasil

**STATUS: LOCK**

---

# PHASE 8 — ATTACHMENT

## Image
- [ ] Picker
- [ ] Preview
- [ ] Upload
- [ ] Download
- [ ] Display

## File
- [ ] File picker
- [ ] Upload
- [ ] Download
- [ ] Metadata

## Video
- [ ] Upload
- [ ] Thumbnail
- [ ] Playback

## Storage
- [ ] Object storage
- [ ] Signed URL
- [ ] Access control
- [ ] Expiration

Setiap subfitur: **IMPLEMENT → TEST → LOCK**.

---

# PHASE 9 — VOICE NOTE

- [ ] Record audio
- [ ] Microphone permission
- [ ] Stop recording
- [ ] Playback
- [ ] Upload
- [ ] Download
- [ ] Audio message bubble
- [ ] Progress
- [ ] Cancel

### TEST
- [ ] Record
- [ ] Send
- [ ] Receive
- [ ] Playback
- [ ] Reopen app
- [ ] Playback kembali

**STATUS: LOCK**

---

# PHASE 10 — PUSH NOTIFICATION

- [ ] Push token
- [ ] Register device
- [ ] Save token
- [ ] Send notification
- [ ] Background notification
- [ ] Notification saat app ditutup
- [ ] Tap notification → buka chat
- [ ] Token refresh

**STATUS: LOCK**

---

# PHASE 11 — VOICE CALL

> Jangan dikerjakan sebelum Chat Core benar-benar LOCK.

## Signaling
- [ ] Call request
- [ ] Accept
- [ ] Reject
- [ ] End
- [ ] Timeout

## WebRTC
- [ ] Peer connection
- [ ] ICE
- [ ] STUN
- [ ] TURN
- [ ] Audio stream

## UI
- [ ] Incoming call
- [ ] Calling
- [ ] Connected
- [ ] Mute
- [ ] Speaker
- [ ] End

### TEST
- [ ] A → B
- [ ] Ring
- [ ] Accept
- [ ] Bicara
- [ ] Mute
- [ ] Speaker
- [ ] End

**STATUS: LOCK**

---

# PHASE 12 — VIDEO CALL

- [ ] Camera permission
- [ ] Local video
- [ ] Remote video
- [ ] Switch camera
- [ ] Mute
- [ ] Disable camera
- [ ] Speaker
- [ ] End call
- [ ] Network recovery

**STATUS: LOCK**

---

# PHASE 13 — STATUS / STORY

## Status
- [ ] Create status
- [ ] Text
- [ ] Image
- [ ] Video
- [ ] Status list
- [ ] Viewed/unviewed
- [ ] Viewer
- [ ] 24-hour expiration
- [ ] Delete status

## Backend
- [ ] Status table
- [ ] Media
- [ ] Views
- [ ] Expiration cleanup

**STATUS: LOCK**

---

# PHASE 14 — WORK SPACE

> Dikerjakan setelah Messenger Core stabil.

## Workspace
- [ ] Workspace
- [ ] Members
- [ ] Roles
- [ ] Permissions
- [ ] Workspace chat

## Tools
- [ ] Remote
- [ ] Scan dokumen
- [ ] Fingerprint/biometric lock
- [ ] Location
- [ ] Convert image
- [ ] Print document
- [ ] Screen sharing
- [ ] MP3 player

Setiap tool: **SATU TOOL → TEST → LOCK**.

---

# PHASE 15 — SECURITY HARDENING

- [ ] HTTPS
- [ ] WSS
- [ ] Secure token handling
- [ ] Password hashing
- [ ] Session expiration
- [ ] Authorization
- [ ] Rate limiting
- [ ] Input validation
- [ ] SQL injection protection
- [ ] File validation
- [ ] Access control
- [ ] Secure local storage
- [ ] Biometric protection
- [ ] Logging tanpa password/token

**STATUS: LOCK**

---

# PHASE 16 — END-TO-END ENCRYPTION

- [ ] Identity key
- [ ] Session key
- [ ] Message encryption
- [ ] Message decryption
- [ ] Key rotation
- [ ] Device management
- [ ] Secure key storage
- [ ] Recovery strategy

### TEST
- [ ] A → B
- [ ] Server tidak menyimpan plaintext message
- [ ] B dapat decrypt
- [ ] Key/session behavior diuji
- [ ] Multi-device diuji

**STATUS: LOCK**

---

# PHASE 17 — MULTI-DEVICE

- [ ] Device registration
- [ ] Device ID
- [ ] Multiple sessions
- [ ] Message sync
- [ ] Push per device
- [ ] Logout one device
- [ ] Logout all devices
- [ ] Device management

**STATUS: LOCK**

---

# PHASE 18 — PRODUCTION BACKEND

- [ ] Worker production
- [ ] D1 production
- [ ] D1 migrations
- [ ] Storage production
- [ ] Environment secrets
- [ ] Domain
- [ ] HTTPS
- [ ] WSS
- [ ] Monitoring
- [ ] Error logging
- [ ] Backup/recovery

Gunakan migration SQL berurutan untuk perubahan schema D1 agar perubahan database dapat dilacak dan diterapkan dengan aman. Cloudflare mendokumentasikan migration versioning dan `wrangler d1 migrations apply` untuk alur ini.

**STATUS: LOCK**

---

# PHASE 19 — ANDROID PRODUCTION

- [ ] App icon
- [ ] Splash screen
- [ ] Production package
- [ ] Release signing
- [ ] ProGuard/R8
- [ ] Crash reporting
- [ ] Network security
- [ ] Background strategy
- [ ] Push notification
- [ ] Battery optimization handling
- [ ] Permission handling

**STATUS: LOCK**

---

# PHASE 20 — FULL TESTING

## Authentication
- [ ] Register
- [ ] Login
- [ ] Logout
- [ ] Session

## Chat
- [ ] Send
- [ ] Receive
- [ ] Reply
- [ ] Forward
- [ ] Delete
- [ ] Read
- [ ] Unread
- [ ] Search

## Network
- [ ] WiFi
- [ ] Mobile data
- [ ] Slow network
- [ ] Offline
- [ ] Reconnect

## Device
- [ ] Device 1
- [ ] Device 2
- [ ] Background
- [ ] App killed
- [ ] Device restart

## Security
- [ ] Invalid token
- [ ] Unauthorized conversation
- [ ] Invalid request
- [ ] Rate limit
- [ ] File validation

**STATUS: LOCK**

---

# PHASE 21 — RELEASE 1.0

- [ ] Version 1.0
- [ ] Release build
- [ ] APK test
- [ ] AAB build
- [ ] Real device test
- [ ] Production backend
- [ ] Final DB migration
- [ ] Backup
- [ ] Monitoring
- [ ] Release notes
- [ ] Documentation

---

# URUTAN BESAR

```text
01. FONDASI PROJECT
        ↓
02. AUTHENTICATION
        ↓
03. USER PROFILE
        ↓
04. CONTACTS
        ↓
05. 1-TO-1 CHAT
        ↓
06. REAL-TIME CHAT
        ↓
07. REPLY / FORWARD / DELETE / UNREAD / SEARCH
        ↓
08. LOCAL CACHE + OFFLINE SYNC
        ↓
09. ATTACHMENT
        ↓
10. VOICE NOTE
        ↓
11. PUSH NOTIFICATION
        ↓
12. VOICE CALL
        ↓
13. VIDEO CALL
        ↓
14. STATUS / STORY
        ↓
15. WORK SPACE
        ↓
16. SECURITY HARDENING
        ↓
17. END-TO-END ENCRYPTION
        ↓
18. MULTI-DEVICE
        ↓
19. PRODUCTION BACKEND
        ↓
20. FULL TEST
        ↓
21. RELEASE 1.0
```

# WORKFLOW WAJIB

```text
IMPLEMENT
   ↓
BUILD
   ↓
TEST
   ↓
FIX
   ↓
RETEST
   ↓
PASS
   ↓
LOCK
   ↓
NEXT FEATURE
```

## Larangan

Jangan:

```text
Chat belum selesai
↓
lompat Call
↓
lompat Story
↓
balik Chat
↓
ubah fondasi
```

Yang benar:

```text
FEATURE A
→ SELESAI
→ TEST
→ LOCK

↓

FEATURE B
→ SELESAI
→ TEST
→ LOCK

↓

FEATURE C
→ ...
```

# TARGET AKHIR

WAW ditargetkan menjadi aplikasi Android messenger nyata dengan:

- Chat real-time
- Authentication
- User profile
- Contacts
- Persistent messages
- Delivery/read status
- Offline sync
- Reply
- Forward
- Delete
- Attachment
- Voice note
- Push notification
- Voice call
- Video call
- Status
- Workspace
- Security
- E2E encryption
- Multi-device
- Production backend
- APK/AAB production

**PRINSIP:**

> Jangan mengejar banyak fitur sekaligus. Kejar satu fitur yang benar-benar selesai, dites, dan dikunci.
