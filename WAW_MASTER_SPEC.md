# WAW Messenger + WAW Workspace — Master Specification

> Dokumen induk tunggal untuk arah produk, batasan teknis, fitur, dan urutan implementasi project WAW.

## 1. Tujuan produk

WAW adalah aplikasi Android dengan dua bagian yang terhubung:

- **WAW Messenger** — messenger mandiri dengan pengalaman seperti WhatsApp: akun, kontak, chat 1:1, grup, pesan realtime, media, status, notifikasi, dan panggilan.
- **WAW Workspace** — toolbox produktivitas dan keamanan: file manager, dokumen/PDF, scanner, notes, tasks, secure vault, network diagnostics, backup, pencarian universal, dan remote-device management yang diotorisasi.

Sasaran utama: pengguna dapat memakai satu aplikasi untuk komunikasi WAW dan pekerjaan sehari-hari, dengan data serta izin yang jelas.

## 2. Dua mode koneksi yang harus dibedakan

### A. WAW account — fitur penuh

Pengguna WAW saling terhubung melalui akun WAW. Di mode ini kita dapat membangun dan mengontrol:

- chat pribadi dan grup;
- pesan teks, emoji, reply, forward, edit, hapus, pin, star, search;
- status pesan: sending, sent, delivered, read, failed;
- online/last seen, typing indicator, mute, archive, block, report;
- foto, video, dokumen, lokasi, contact card, voice note;
- status/updates 24 jam;
- voice call dan video call antarpengguna WAW melalui WebRTC dengan signaling resmi milik WAW;
- push notification, multi-device session, backup, biometric lock, dan kontrol privasi.

### B. WhatsApp official integration — terbatas dan patuh aturan

Koneksi ke **pengguna WhatsApp asli** hanya boleh memakai produk/API resmi Meta/WhatsApp yang tersedia dan diizinkan untuk use case tersebut. Integrasi resmi umumnya berfokus pada akun bisnis, customer messaging, template message, webhook, dan inbox bisnis.

WAW **tidak boleh** mengambil alih akun WhatsApp pribadi, membaca database lokal WhatsApp, meminta password/OTP, mencuri token/cookie, meniru client resmi, memakai protokol privat, atau mengklaim bahwa chat pribadi, status pribadi, voice call, dan video call WhatsApp sudah terhubung jika API resmi tidak menyediakan kemampuan itu.

Jika kemampuan WhatsApp resmi tidak tersedia, UI harus menampilkan status NOT_SUPPORTED_BY_OFFICIAL_API, bukan fallback tersembunyi atau bypass.

## 3. Modul WAW Messenger

### Identity dan account

- Register, login, logout, session expiry, password recovery.
- Username unik, display name, avatar, email verification, device/session list.
- Secure token storage dan biometric gate.
- Delete account, export data, revoke all sessions.

### Contacts dan discovery

- Search pengguna WAW berdasarkan username/email yang diizinkan.
- Contact request, accept/reject, block/report.
- Presence: online, offline, last seen, typing.
- Privacy controls per pengguna.

### Chat dan group

- Direct conversation dan group conversation.
- Pagination/history sync, retry queue, idempotent client message ID.
- Reply, edit, delete-for-me, delete-for-everyone sesuai policy.
- Read receipts, delivery receipts, unread count, mute, archive, pin.
- Group roles, invite, remove member, permissions, group photo/name/description.

### Media

- Upload aman dengan signed URL dan batas ukuran.
- Thumbnail/image compression, video preview, document download.
- Voice note dengan duration, waveform, playback speed.
- Progress, retry, cancel, offline queue, dan virus/content validation di server.

### Status/Updates

- Status teks, foto, video, dan voice.
- Expire otomatis setelah 24 jam.
- Viewer list, privacy audience, mute, delete.
- Status hanya untuk pengguna WAW kecuali ada capability resmi WhatsApp yang benar-benar mendukungnya.

### Voice dan video call

- Signaling server WAW, WebRTC peer connection, ICE/STUN/TURN.
- 1:1 voice/video terlebih dahulu; group call setelah reliability dan monitoring siap.
- Call invite, ringing, accept, reject, busy, ended, reconnect, mute, camera switch, speaker.
- Permission, network fallback, call quality metrics, abuse/rate limit.
- Jangan menyebut fitur ini sebagai WhatsApp call; ini adalah WAW call kecuali tersedia integrasi resmi.

### Notifications dan reliability

- Push notification untuk pesan, mention, call, dan status.
- Reconnect exponential backoff.
- Realtime WebSocket sebagai jalur cepat dengan HTTP sync/polling sebagai fallback.
- Background sync hanya dengan session valid dan permission yang sesuai.

## 4. Modul WAW Workspace

- **Workspace home:** universal search, recent items, quick actions.
- **File manager:** folder, rename, move, copy, delete, share, upload/download.
- **Document/PDF:** editor dasar, preview, merge/split, export, watermark.
- **Scanner:** camera scan, crop, enhance, image-to-PDF.
- **Notes + Tasks:** notes, checklist, due date, labels, reminders.
- **Secure Vault:** encrypted local data, biometric lock, timeout, recovery policy.
- **Network diagnostics:** connectivity, IP information, latency, DNS checks; tidak mengklaim lokasi GPS dari IP.
- **WAW Shield:** anti-phishing/anti-judol warnings berbasis rules dan sumber yang jelas.
- **Backup + sync:** encrypted backup, restore, conflict handling, device list.
- **Remote device:** pairing eksplisit, device approval, revoke, audit log; tidak ada akses diam-diam.

## 5. Arsitektur target

- **Android:** Kotlin, Jetpack Compose, WorkManager, OkHttp, secure storage, biometric gate.
- **Backend API:** Cloudflare Worker dengan D1 untuk identity, conversations, messages, receipts, status, dan workspace metadata.
- **Realtime:** authenticated WebSocket untuk event cepat; HTTP sync sebagai sumber kebenaran dan fallback. Untuk skala multi-instance, gunakan Durable Object/pub-sub atau layanan realtime yang teruji sebelum mengklaim realtime global.
- **Media:** object storage dengan signed upload/download URL; metadata dan permission tetap di database.
- **Calls:** signaling WAW + WebRTC + STUN/TURN; jangan menyimpan rekaman tanpa opt-in yang jelas.
- **Security:** password hashing, short-lived access/session policy, token hashing server-side, least privilege, rate limit, audit log, no secrets in repository.

## 6. Data inti

Minimum entity: users, sessions, devices, contacts, blocks, conversations, conversation_members, messages, message_receipts, attachments, statuses, status_views, calls, workspace_items, workspace_shares, push_tokens, dan audit_events.

Semua operasi pesan harus:

1. memeriksa session dan membership;
2. memvalidasi ukuran/isi;
3. menerima clientId idempotent;
4. menyimpan ke database sebelum mengirim event;
5. mengirim event ke anggota yang berhak;
6. tetap dapat dipulihkan lewat sync setelah offline.

## 7. Aturan integrasi WhatsApp resmi

- Tidak meminta pengguna memasukkan password atau OTP WhatsApp ke WAW.
- Tidak mengimpor database/session/token WhatsApp.
- Tidak mengakses pesan/status/kontak pribadi jika API resmi tidak mengizinkan.
- Gunakan akun bisnis, access token, webhook, template, dan consent sesuai dokumentasi resmi jika fitur itu diperlukan.
- Pisahkan identitas WAW user dan WhatsApp business account di database.
- Setiap capability harus memiliki status AVAILABLE, DEGRADED, atau NOT_SUPPORTED_BY_OFFICIAL_API.

## 8. Urutan implementasi

1. **Foundation:** auth, session, database migration, API error model, secure storage.
2. **Chat core:** direct chat, message persistence, idempotency, receipts, unread count, reliable sync.
3. **Realtime:** authenticated event delivery, reconnect, presence, typing, push notifications.
4. **Media dan status:** attachment pipeline, voice notes, status expiry/privacy.
5. **Group:** roles, permissions, invite, moderation.
6. **WAW calls:** WebRTC 1:1 voice/video, then group call.
7. **Workspace:** file manager, docs/PDF, scanner, notes/tasks, vault, diagnostics, backup.
8. **Official WhatsApp business connector:** only capabilities approved by official API, with explicit UI labels.
9. **Hardening:** abuse prevention, observability, load/reconnect tests, backup restore, release signing, privacy review.

## 9. Definition of done

Sebuah fitur dianggap selesai bila:

- happy path dan failure path sudah diuji;
- data tetap konsisten setelah retry, offline, dan reconnect;
- permission dan privacy copy jelas;
- tidak ada credential/protocol privat yang digunakan;
- backend menolak unauthorized access dan cross-conversation access;
- Android build, backend test, dan deployment check berhasil;
- status capability di UI sesuai kemampuan nyata, bukan janji marketing.

## 10. Status implementasi saat dokumen dibuat

- Auth/session dan fondasi chat sudah ada.
- Backend chat sudah menyimpan pesan dengan client ID idempotent.
- Pengiriman Android diperbaiki agar pesan dipersist melalui HTTP sebelum refresh/realtime delivery.
- Event WebSocket sekarang dipancarkan ke sesi anggota percakapan yang terhubung, dengan polling/sync sebagai fallback.
- Fitur media, status 24 jam, push notification penuh, WebRTC call, dan Workspace lengkap masih harus diimplementasikan bertahap sesuai urutan di atas.
- Koneksi ke akun WhatsApp pribadi tidak dijanjikan karena harus mengikuti kemampuan API resmi yang tersedia.
