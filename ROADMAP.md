# WAW — MASTER ROADMAP

## TARGET UTAMA

WAW ditujukan sebagai aplikasi Android yang mengikuti **model WhatsApp asli**, dengan fokus utama pada konsep **linked/companion device** terhadap akun WhatsApp yang sah.

WAW **bukan** messenger mandiri dengan akun WAW + backend chat sendiri sebagai jalur utama.

Target arsitektur:

```text
WhatsApp Primary Phone
        │
        │  Link Device / Companion Device
        │  QR / metode linking resmi
        ▼
      WAW Android
        │
        │  sesi perangkat tertaut
        ▼
 WhatsApp account/session
        │
        ├── Chat
        ├── Contacts
        ├── Groups
        ├── Media
        ├── Notifications
        ├── Calls
        └── Device management
```

### ATURAN PALING PENTING

1. WAW harus mengikuti **mekanisme resmi yang tersedia** untuk linked/companion device.
2. Jangan membuat server WAW sebagai pengganti jaringan WhatsApp.
3. Jangan meminta password WhatsApp pengguna.
4. Jangan mengambil, menyalin, atau membundel credential, token, cookie, private key, atau session rahasia WhatsApp.
5. Jangan bypass, spoof, replay, atau melemahkan kontrol keamanan WhatsApp.
6. Jangan reverse-engineer protokol privat untuk menghindari mekanisme resmi.
7. Bila suatu kemampuan tidak tersedia melalui mekanisme resmi, fitur tersebut diberi status `BLOCKED / NOT_SUPPORTED`, bukan dipaksa melalui bypass.
8. Semua pengujian menggunakan akun dan perangkat yang memang berhak digunakan oleh penguji.
9. Jangan menganggap fitur selesai hanya karena UI sudah tampil.
10. Setiap tahap wajib: **IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK → NEXT**.

WhatsApp mendokumentasikan linked devices, termasuk companion phones, serta proses linking melalui perangkat utama. WhatsApp juga menyatakan bahwa perangkat tertaut dapat bekerja tanpa telepon utama tetap online, dengan batasan dan aturan koneksi tertentu. Dokumentasi resmi harus menjadi sumber acuan ketika menentukan perilaku WAW. 

---

# 0. PRODUCT DEFINITION

## Identitas

- [✓] Nama project: WAW
- [✓] Repository: `frostbyte-lab/waw-messenger`
- [ ] Android production identity
- [ ] Product documentation
- [ ] Architecture documentation

## Prinsip Produk

WAW harus terasa seperti **client WhatsApp yang sah dan aman**, bukan aplikasi yang membuat jaringan messaging baru.

### Yang menjadi sumber kebenaran

```text
WhatsApp account
      ↓
WhatsApp linked-device state
      ↓
WAW local state/UI
```

Bukan:

```text
WAW database
      ↓
menjadi sumber kebenaran akun WhatsApp
```

---

# 1. OFFICIAL LINKED / COMPANION DEVICE

Ini adalah fase paling penting dan harus selesai sebelum membangun fitur chat sebagai fitur utama.

## Device Linking

- [ ] Tentukan mekanisme linking resmi yang tersedia untuk target WAW
- [ ] QR linking bila didukung untuk target perangkat
- [ ] Phone-number/code linking bila didukung untuk target perangkat
- [ ] Tampilkan status `Waiting for link`
- [ ] Tampilkan status `Linked`
- [ ] Tampilkan status `Expired`
- [ ] Tampilkan status `Disconnected`
- [ ] Unlink/logout
- [ ] Device name
- [ ] Device identity lokal
- [ ] Device state persistence

## Primary Phone Flow

```text
PRIMARY PHONE
   ↓
WhatsApp
   ↓
Linked Devices
   ↓
Link Device
   ↓
Authorize WAW
   ↓
WAW menjadi perangkat tertaut
```

## Security

- [ ] Tidak meminta password WhatsApp
- [ ] Tidak menyimpan OTP WhatsApp sebagai credential permanen
- [ ] Tidak mengekstrak session dari aplikasi WhatsApp
- [ ] Tidak menyimpan cookie/session rahasia WhatsApp
- [ ] Secure local key storage
- [ ] Device unlink support
- [ ] Local app lock

### TEST WAJIB

- [ ] Link WAW ke akun penguji
- [ ] Primary phone menampilkan WAW sebagai perangkat tertaut
- [ ] WAW mendapatkan state linked
- [ ] Restart WAW
- [ ] Session/device state tetap valid sesuai mekanisme resmi
- [ ] Unlink dari primary phone
- [ ] WAW mendeteksi disconnected
- [ ] Link ulang berhasil

**STATUS: LOCK setelah seluruh test lulus.**

---

# 2. ACCOUNT / IDENTITY SYNC

Tidak menggunakan `username + password` WAW sebagai identitas utama.

## Identity

- [ ] WhatsApp account identity
- [ ] Phone number identity sesuai data yang diberikan oleh mekanisme resmi
- [ ] Device identity
- [ ] Profile name
- [ ] Profile photo/avatar
- [ ] About/status profile jika tersedia
- [ ] Online/offline state sesuai data yang tersedia
- [ ] Last seen sesuai izin/availability

## Local State

- [ ] Linked account ID/reference
- [ ] Linked device ID/reference
- [ ] Session state
- [ ] Sync cursor/state bila mekanisme resmi menyediakannya
- [ ] Local encrypted storage

### TEST

- [ ] Account tampil benar
- [ ] Profile berubah di WhatsApp → WAW mengikuti sinkronisasi yang tersedia
- [ ] Unlink → local account state dibersihkan dengan aman

**STATUS: LOCK**

---

# 3. CONTACTS

Kontak mengikuti model WhatsApp, bukan tabel contact WAW yang berdiri sendiri sebagai sumber kebenaran.

- [ ] Permission contacts bila diperlukan
- [ ] Contact sync
- [ ] Phone-number identity
- [ ] Contact name
- [ ] Profile photo
- [ ] Search contact
- [ ] Start chat
- [ ] Blocked contact state bila tersedia
- [ ] Contact refresh/sync

### TEST

- [ ] Kontak tersinkron
- [ ] Cari nomor
- [ ] Buka profile
- [ ] Mulai chat
- [ ] Perubahan contact tidak merusak identity

**STATUS: LOCK**

---

# 4. CHAT CORE

## Conversation List

- [ ] Chat list
- [ ] Pinned chat bila tersedia
- [ ] Archived chat bila tersedia
- [ ] Unread count
- [ ] Last message preview
- [ ] Timestamp
- [ ] Mute state bila tersedia
- [ ] Search chat

## Message

- [ ] Text message
- [ ] Sender
- [ ] Receiver/group context
- [ ] Message ID/reference
- [ ] Timestamp
- [ ] Sending state
- [ ] Sent state
- [ ] Delivered state
- [ ] Read state
- [ ] Failed state
- [ ] Retry

## Chat Screen

- [ ] Header
- [ ] Contact/group information
- [ ] Message list
- [ ] Composer
- [ ] Send button
- [ ] Attachment button
- [ ] Voice button
- [ ] Scroll position
- [ ] Date separator
- [ ] Unread separator

### TEST WAJIB

```text
ACCOUNT A
   ↓
WAW
   ↓
WhatsApp network
   ↓
ACCOUNT B
```

- [ ] A mengirim ke B
- [ ] B menerima
- [ ] B membalas A
- [ ] Status SENT
- [ ] Status DELIVERED
- [ ] Status READ
- [ ] Tutup WAW
- [ ] Buka WAW
- [ ] Chat tetap tersinkron sesuai kemampuan linked device

**STATUS: LOCK**

---

# 5. MESSAGE HISTORY & SYNC

- [ ] Initial history sync
- [ ] Incremental sync
- [ ] Message pagination
- [ ] Local cache
- [ ] Sync cursor
- [ ] Duplicate prevention
- [ ] Missing-message recovery
- [ ] Timestamp ordering
- [ ] Conversation ordering
- [ ] Reconnect sync

### Catatan

Jangan membuat history palsu dari D1. History harus berasal dari state/data yang memang diberikan oleh sistem WhatsApp melalui mekanisme yang sah.

**STATUS: LOCK**

---

# 6. CHAT FEATURES

Kerjakan satu per satu: **IMPLEMENT → TEST → LOCK**.

## Reply

- [ ] Reply message
- [ ] Quoted preview
- [ ] Jump to original

## Forward

- [ ] Select message
- [ ] Select destination
- [ ] Forward
- [ ] Preserve appropriate metadata

## Delete

- [ ] Delete for me
- [ ] Delete for everyone bila tersedia
- [ ] Permission/state validation
- [ ] Deleted placeholder

## Reactions

- [ ] Add reaction
- [ ] Remove reaction
- [ ] Reaction display
- [ ] Sync

## Search

- [ ] Search chats
- [ ] Search messages
- [ ] Highlight result
- [ ] Open result

**STATUS: LOCK**

---

# 7. MEDIA & ATTACHMENTS

## Image

- [ ] Image picker
- [ ] Preview
- [ ] Send
- [ ] Receive
- [ ] Download/cache
- [ ] Fullscreen viewer

## Video

- [ ] Picker
- [ ] Preview
- [ ] Send
- [ ] Receive
- [ ] Playback

## Document

- [ ] File picker
- [ ] Send
- [ ] Receive
- [ ] File metadata
- [ ] Open/share

## Audio

- [ ] Audio message
- [ ] Playback
- [ ] Progress
- [ ] Pause/resume

Media transport must follow the official supported linked-device behavior. Do not implement private-media endpoints by guessing or replaying undocumented credentials.

**STATUS: LOCK**

---

# 8. VOICE NOTE

- [ ] Microphone permission
- [ ] Record
- [ ] Cancel
- [ ] Preview/playback
- [ ] Send
- [ ] Receive
- [ ] Progress
- [ ] Retry

### TEST

- [ ] Record A
- [ ] Send A → B
- [ ] Receive B
- [ ] Playback B
- [ ] Reopen app
- [ ] Message remains synchronized

**STATUS: LOCK**

---

# 9. GROUP CHAT

- [ ] Group list
- [ ] Group profile
- [ ] Group members
- [ ] Group messages
- [ ] Add participant where supported
- [ ] Remove participant where supported
- [ ] Admin state
- [ ] Group invite/state where supported
- [ ] Group media
- [ ] Group sync

**STATUS: LOCK**

---

# 10. NOTIFICATION

- [ ] New message notification
- [ ] Group notification
- [ ] Mention notification
- [ ] Notification grouping
- [ ] Tap notification → open chat
- [ ] Background state
- [ ] App killed state
- [ ] Notification permission
- [ ] Device token handling

Notification implementation must not create a second unofficial messaging backend.

**STATUS: LOCK**

---

# 11. VOICE CALL

Only implement capabilities that are officially available to the target linked/companion device model.

- [ ] Incoming call UI
- [ ] Outgoing call UI
- [ ] Ringing
- [ ] Accept
- [ ] Reject
- [ ] End
- [ ] Mute
- [ ] Speaker
- [ ] Network recovery

### TEST

- [ ] WAW → WhatsApp account
- [ ] WhatsApp account → WAW
- [ ] Ring
- [ ] Accept
- [ ] Audio
- [ ] Mute
- [ ] End

**STATUS: LOCK**

---

# 12. VIDEO CALL

- [ ] Camera permission
- [ ] Incoming video call
- [ ] Outgoing video call
- [ ] Local video
- [ ] Remote video
- [ ] Switch camera
- [ ] Mute
- [ ] Disable camera
- [ ] Speaker
- [ ] End
- [ ] Network recovery

**STATUS: LOCK**

---

# 13. STATUS / STORY

Implement only features officially available to the selected linked/companion device type.

- [ ] Status list
- [ ] Text status where supported
- [ ] Image status where supported
- [ ] Video status where supported
- [ ] Viewed/unviewed
- [ ] Viewer information where supported
- [ ] Expiration
- [ ] Delete
- [ ] Sync

**STATUS: LOCK**

---

# 14. MULTI-DEVICE MANAGEMENT

- [ ] Current device information
- [ ] Linked-device list if available
- [ ] Device name
- [ ] Device status
- [ ] Last active
- [ ] Unlink current device
- [ ] Detect remote unlink
- [ ] Re-link
- [ ] Session recovery

WhatsApp currently documents support for multiple linked devices and companion phones. The exact capabilities available to WAW must be verified against the official documentation and the actual target device category before implementation. 

**STATUS: LOCK**

---

# 15. LOCAL STORAGE & SECURITY

## Secure Storage

- [ ] Android Keystore
- [ ] Encrypted local database/cache
- [ ] No plaintext secret storage
- [ ] No WhatsApp password storage
- [ ] No raw OTP storage
- [ ] No exported session secrets
- [ ] App lock / biometric lock

## Data Handling

- [ ] Minimize stored data
- [ ] Secure deletion
- [ ] Log sanitization
- [ ] No tokens in logs
- [ ] No credentials in logs
- [ ] No personal data in debug logs

**STATUS: LOCK**

---

# 16. NETWORK / TRANSPORT

- [ ] HTTPS where applicable
- [ ] TLS validation
- [ ] Official endpoint/interface only
- [ ] Network timeout
- [ ] Retry policy
- [ ] Reconnect
- [ ] Offline state
- [ ] Connection state UI
- [ ] Network error handling

### DILARANG

```text
Unknown endpoint
      ↓
Guess request
      ↓
Replay token
      ↓
Bypass auth
```

Jika interface resmi tidak menyediakan suatu operasi, tandai sebagai `NOT_SUPPORTED`.

---

# 17. BACKEND WAW

Backend WAW **bukan pengganti backend WhatsApp**.

Backend milik WAW hanya boleh digunakan untuk fungsi yang memang merupakan milik WAW, misalnya:

- [ ] Crash/diagnostic telemetry dengan privasi yang sesuai
- [ ] Remote configuration WAW
- [ ] Release metadata
- [ ] Optional WAW workspace features
- [ ] Operational monitoring

Jangan membuat:

```text
WAW D1
 ├── users WhatsApp
 ├── WhatsApp passwords
 ├── WhatsApp sessions
 ├── WhatsApp messages
 └── WhatsApp private keys
```

Database WAW tidak boleh menjadi tempat penyalinan database internal WhatsApp.

**STATUS: LOCK**

---

# 18. OLD STANDALONE CHAT CODE

Kode lama yang menggunakan:

```text
WAW username
WAW password
WAW sessions
WAW conversations
WAW messages
WAW WebSocket
Cloudflare D1 sebagai chat authority
```

harus diperlakukan sebagai **legacy/prototype architecture**, bukan target utama produk.

- [ ] Audit `AuthRepository`
- [ ] Audit `ChatRepository`
- [ ] Audit `WebSocketChatRepository`
- [ ] Audit D1 chat schema
- [ ] Tentukan kode yang masih berguna
- [ ] Jangan hapus sebelum pengganti tervalidasi
- [ ] Pisahkan legacy dari production path

**Tidak boleh melakukan penghapusan massal.**

---

# 19. UI / UX

WAW mengikuti pola UX WhatsApp secara fungsional, tetapi tidak menyalin asset/kode proprietari WhatsApp.

## Main Navigation

- [ ] Chats
- [ ] Updates/Status bila didukung
- [ ] Calls bila didukung
- [ ] Settings

## Chat UI

- [ ] Header
- [ ] Avatar
- [ ] Online/last seen state
- [ ] Message bubbles
- [ ] Check marks/status
- [ ] Composer
- [ ] Attachments
- [ ] Voice
- [ ] Search
- [ ] Context actions

## Design Rule

```text
FUNCTIONAL PARITY
       ≠
COPY PROPRIETARY CODE/ASSET
```

WAW menggunakan implementasi UI native milik sendiri.

**STATUS: LOCK**

---

# 20. SECURITY / COMPLIANCE GATE

Release wajib gagal jika ditemukan:

- [ ] Password WhatsApp disimpan
- [ ] OTP disimpan sebagai credential permanen
- [ ] Credential pihak lain
- [ ] Token aktif dibundel
- [ ] Cookie/session rahasia dibundel
- [ ] Private key pihak lain
- [ ] Mekanisme bypass authentication
- [ ] Replay credential/session
- [ ] Endpoint privat yang digunakan tanpa otorisasi
- [ ] Data pribadi yang tidak diperlukan
- [ ] Kode/asset proprietari yang tidak berizin

Release hanya boleh lanjut jika:

- [ ] Semua credential/secret dibersihkan
- [ ] Permission/scope terdokumentasi
- [ ] Interface resmi/berizin teridentifikasi
- [ ] Security scan PASS
- [ ] Secret scan PASS
- [ ] Test linking PASS
- [ ] Test unlink PASS
- [ ] Test chat PASS
- [ ] Audit trail tersedia

---

# 21. TEST MATRIX

## Linking

- [ ] QR/code linking
- [ ] Invalid code
- [ ] Expired code
- [ ] Cancel linking
- [ ] Successful linking
- [ ] Remote unlink
- [ ] Local logout
- [ ] Re-link

## Chat

- [ ] WAW → WhatsApp
- [ ] WhatsApp → WAW
- [ ] Sent
- [ ] Delivered
- [ ] Read
- [ ] Failed
- [ ] Retry
- [ ] Reconnect
- [ ] History sync

## Network

- [ ] Wi-Fi
- [ ] Mobile data
- [ ] Slow network
- [ ] Offline
- [ ] Reconnect
- [ ] App killed
- [ ] Device restart

## Device

- [ ] Android phone
- [ ] Primary phone
- [ ] Linked-device state
- [ ] Battery saver
- [ ] Background restrictions
- [ ] Notification permission

## Security

- [ ] No secret in logs
- [ ] No plaintext credentials
- [ ] Secure storage
- [ ] Remote unlink
- [ ] Invalid session state
- [ ] Unauthorized operation rejected

**STATUS: LOCK setelah seluruh matrix lulus.**

---

# 22. PRODUCTION ANDROID

- [ ] Production application ID
- [ ] App icon
- [ ] Splash screen
- [ ] Release signing
- [ ] R8/ProGuard
- [ ] Secure network configuration
- [ ] Crash reporting
- [ ] Background behavior
- [ ] Notification handling
- [ ] Battery optimization handling
- [ ] Permission handling
- [ ] Real-device test
- [ ] APK
- [ ] AAB

**STATUS: LOCK**

---

# 23. RELEASE CHECKLIST

```text
PRODUCT DEFINITION
      ↓
LINKED DEVICE
      ↓
IDENTITY SYNC
      ↓
CONTACTS
      ↓
CHAT CORE
      ↓
MESSAGE SYNC
      ↓
MEDIA
      ↓
VOICE NOTE
      ↓
GROUP
      ↓
NOTIFICATION
      ↓
CALLS
      ↓
STATUS
      ↓
DEVICE MANAGEMENT
      ↓
SECURITY
      ↓
FULL TEST
      ↓
RELEASE
```

---

# 24. IMPLEMENTATION WORKFLOW WAJIB

```text
PILIH 1 FITUR
      ↓
IMPLEMENT
      ↓
BUILD
      ↓
TEST REAL DEVICE
      ↓
CATAT ERROR
      ↓
FIX
      ↓
RETEST
      ↓
PASS
      ↓
LOCK
      ↓
FITUR BERIKUTNYA
```

Tidak boleh:

```text
Feature A belum PASS
        ↓
Feature B
        ↓
Feature C
        ↓
ubah fondasi lagi
```

---

# 25. CURRENT PRIORITY

Karena target produk telah ditetapkan sebagai **linked/companion device**, prioritas implementasi sekarang adalah:

```text
1. Audit fondasi Android yang ada
        ↓
2. Hentikan penggunaan Auth WAW sebagai jalur utama
        ↓
3. Rancang layar Link Device
        ↓
4. Implementasikan mekanisme linking resmi yang memang tersedia
        ↓
5. Test linking dengan akun penguji
        ↓
6. LOCK LINKING
        ↓
7. Identity/Profile sync
        ↓
8. LOCK IDENTITY
        ↓
9. Chat sync
        ↓
10. LOCK CHAT CORE
```

**Jangan melanjutkan fitur chat mandiri berbasis D1/WebSocket sebagai target production sebelum audit arsitektur ini selesai.**

---

# TARGET AKHIR

WAW harus menjadi aplikasi Android nyata dengan model penggunaan:

```text
Pengguna mempunyai akun WhatsApp
            ↓
Primary WhatsApp phone
            ↓
Authorize / Link WAW
            ↓
WAW menjadi linked/companion device
            ↓
Pengguna mengakses akun yang sama
            ↓
Chat dengan kontak WhatsApp asli
```

Fitur yang ditargetkan, **sejauh resmi didukung oleh tipe perangkat linked/companion yang digunakan**:

- Linked device
- Account/profile
- Contacts
- 1-to-1 chat
- Group chat
- Message sync
- Delivery/read state
- Reply
- Forward
- Delete
- Reactions
- Search
- Image
- Video
- Document
- Audio/voice note
- Notifications
- Voice call
- Video call
- Status/Updates
- Device management
- Secure local storage
- Production Android

## DEFINISI SELESAI

WAW tidak dianggap selesai karena tampil seperti WhatsApp.

WAW dianggap selesai bila fitur yang ditargetkan:

```text
BENAR-BENAR TERHUBUNG
        ↓
BENAR-BENAR TERSINKRON
        ↓
BENAR-BENAR DAPAT DITES
        ↓
AMAN
        ↓
PASS
        ↓
LOCK
```

**PRINSIP FINAL:**

> Ikuti model dan kemampuan WhatsApp yang resmi tersedia. Bangun WAW dengan kode native milik sendiri. Jangan mengganti sistem WhatsApp dengan backend WAW dan jangan melewati mekanisme keamanan WhatsApp.
