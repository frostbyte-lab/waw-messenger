# Audit Teknis WAW Messenger

**Tanggal audit:** 5 September 2026  
**Repositori:** [frostbyte-lab/waw-messenger](https://github.com/frostbyte-lab/waw-messenger)  
**Commit yang diaudit:** `16bd4af` (`feat(ui): wire WAW chat shell as main app surface`)

## Ringkasan Eksekutif

WAW Messenger saat ini **belum merupakan messenger produksi yang memiliki kemampuan setara WhatsApp**. Aplikasi yang tampil sebagai WAW/WhatsApp Workspace terutama merupakan **UI demo Compose dengan data kontak dan pesan hard-coded**. Fitur chat nyata, media, voice note, panggilan suara/video, story/status, push notification, presence, dan sinkronisasi belum terhubung sebagai jalur produksi.

Repository memang memiliki fondasi backend WAW mandiri berupa autentikasi, Cloudflare D1 schema, dan WebSocket prototype. Namun fondasi tersebut belum terhubung ke layar utama Android, belum memiliki integrasi dua akun, belum memiliki migration/deployment verification, dan masih memiliki beberapa bug fungsional serta celah desain keamanan. Dokumentasi repository sendiri menyatakan bahwa jalur WhatsApp linked-device harus memakai mekanisme resmi dan tidak boleh menebak atau memutar ulang protokol privat [1].

> **Kesimpulan:** UI dapat disebut prototype/demo. Produk belum dapat disebut WhatsApp client, belum siap production release, dan belum boleh mengklaim chat, call, story, atau media sudah berjalan end-to-end.

## Status Fitur

| Area | Status audit | Bukti utama | Risiko |
|---|---|---|---|
| Shell UI Android | Ada | `MainActivity` membuka `WawChatShell` [2] | Rendah |
| Daftar chat | Demo statis | Kontak dibuat di `remember { listOf(...) }` [3] | Tinggi |
| Chat teks | Demo lokal | Pesan awal hard-coded; kirim hanya menambah state Compose [4] | Kritis |
| Persistence/history | Belum ada di UI produksi | Tidak ada pemuatan conversation/history dari backend | Kritis |
| WebSocket chat | Prototype backend/client, belum wired | `AuthRepository` dan `WebSocketChatRepository` tidak dipakai dari `MainActivity` [5] | Kritis |
| Media/attachment | Animasi placeholder | Lampiran selalu menampilkan `preview_design.mp4` dan progress lokal [4] | Tinggi |
| Voice note | Waveform UI saja | Tidak ada `MediaRecorder`, encoder, upload, atau playback [4] | Tinggi |
| Voice call | Belum ada | Ikon telepon tidak memiliki handler; bagian Calls hanya teks placeholder [4] | Kritis |
| Video call | Belum ada | Ikon video memiliki `onClick = {}`; tidak ada WebRTC/signaling [4] | Kritis |
| Story/status | Belum ada | `StatusBody` hanya `SimpleSection` dengan teks statis [4] | Tinggi |
| Presence/typing | Simulasi | Status `online` dan typing berasal dari data lokal serta timer [3][4] | Tinggi |
| Push notification | Belum ada | Tidak ada FCM/push provider/notification channel | Tinggi |
| Groups | Belum ada implementasi UI/backend | Schema hanya fondasi conversation membership, tanpa API group management | Tinggi |
| Reactions/reply/forward | Model sebagian kecil saja | `replyToId` ada pada model, tetapi UI/transport tidak memakainya [6] | Sedang/Tinggi |
| Read/delivery receipts | Belum berjalan | Client `markRead` kosong; backend belum mengirim ACK [5][7] | Tinggi |
| Security local | Sebagian ada | Android Keystore dan biometric gate tersedia [8] | Sedang |
| Security auth | Belum cukup | Bearer token disimpan di SharedPreferences biasa [9] | Tinggi |
| Release build | Belum siap | CI hanya membangun dan mengunggah debug APK [10] | Tinggi |

## Temuan Kritis

### 1. Jalur produksi tidak memakai backend atau repository chat

`MainActivity` hanya merender `WawChatShell` setelah biometric gate berhasil; tidak ada inisialisasi `AuthRepository`, login flow, `ChatRepository`, atau WebSocket session [2]. Di dalam `WawChatShell`, kontak dan pesan dibuat lokal menggunakan `remember` dan `mutableStateListOf`; pesan yang dikirim hanya mengalami transisi status lokal dari `SENDING` ke `SENT`, `DELIVERED`, lalu `READ` menggunakan `delay` [4]. Tidak ada request jaringan, database lokal, atau sinkronisasi.

**Dampak:** pesan hilang saat aplikasi ditutup, tidak ada penerima nyata, status terkirim tidak bermakna, dan UI dapat menyesatkan pengguna seolah-olah pesan sudah tersampaikan.

**Perbaikan:** pisahkan `DemoChatRepository` dari `ProductionChatRepository`, lalu injeksikan repository produksi melalui ViewModel. UI harus menampilkan status `OFFLINE`, `QUEUED`, `SENT`, `DELIVERED`, `READ`, atau `FAILED` berdasarkan ACK server, bukan timer.

### 2. WebSocket client tidak mengirim Authorization header

Backend mengharuskan bearer token pada `/ws` melalui `currentUser` [11]. Namun `WebSocketChatRepository.connect()` membangun request hanya dengan URL dan tidak menambahkan header Authorization [5]. Dengan demikian, client prototype akan ditolak backend sebagai `UNAUTHORIZED` walaupun token tersedia di `AuthRepository`.

**Dampak:** jalur chat nyata tidak dapat melakukan handshake terautentikasi.

**Perbaikan:** buat `WebSocketChatRepository` menerima token provider, tambahkan `Authorization: Bearer <token>` pada request, tangani token kedaluwarsa, dan lakukan reconnect setelah refresh session.

### 3. Backend insert message memiliki jumlah placeholder SQL yang tidak cocok

Backend mendefinisikan delapan kolom pada `INSERT INTO messages`: `id, conversation_id, sender_id, client_id, text, status, created_at, updated_at`. Query values berisi lima placeholder, literal `'SENT'`, lalu tiga placeholder, sehingga jumlah nilai menjadi sembilan untuk delapan kolom [11]. Ini berpotensi membuat setiap pengiriman pesan gagal di D1.

**Perbaikan yang diperlukan:** ubah query menjadi `VALUES (?,?,?,?,?,'SENT',?,?)`, atau gunakan delapan nilai bind secara eksplisit. Tambahkan integration test yang benar-benar menjalankan insert terhadap D1 test database.

### 4. WebSocket hanya mengirim echo ke pengirim dan tidak melakukan fan-out

Setelah insert, backend memanggil `server.send(...)` pada socket pengirim saat ini [11]. Tidak ada registry koneksi per user, broadcast ke anggota conversation, Durable Object, queue, atau mekanisme delivery. Karena itu penerima lain tidak akan mendapatkan pesan secara real time.

**Perbaikan:** gunakan Durable Object atau service koneksi yang resmi untuk mengelola sesi WebSocket, simpan membership, fan-out event, ACK, reconnect cursor, dan idempotency berdasarkan `client_id`.

### 5. Media, voice note, call, dan story belum memiliki backend

Tidak ditemukan endpoint upload/download, object storage R2, metadata attachment, transcoding audio/video, `MediaRecorder`, playback audio, WebRTC, STUN/TURN, call signaling, story entity, expiry job, atau notification event. Yang terlihat sebagai attachment dan waveform hanya animasi lokal pada UI [4].

**Perbaikan:** tetapkan keputusan produk terlebih dahulu. Jika targetnya messenger WAW-owned, bangun media pipeline dan signaling sendiri. Jika targetnya linked device WhatsApp, hanya gunakan mekanisme resmi dan tandai kemampuan yang tidak tersedia sebagai `BLOCKED / NOT_SUPPORTED`, sesuai kebijakan repository [1]. Jangan membuat protokol privat atau mengambil kredensial/session WhatsApp.

## Temuan Keamanan dan Privasi

### Token autentikasi disimpan di SharedPreferences biasa

`AuthRepository` menyimpan bearer token dan expiry di `SharedPreferences` bernama `waw_auth` [9]. Meskipun Android sandbox membatasi akses aplikasi lain, penyimpanan ini tidak memenuhi standar perlindungan yang sudah tersedia di repository karena `SecureStore` berbasis Android Keystore tidak digunakan untuk session token [8].

**Rekomendasi:** simpan token hanya melalui `SecureStore`, gunakan rotasi/revocation, hapus token saat expiry, dan jangan mencatat token ke log atau crash report.

### CORS terlalu permisif dan response normal tidak membawa header CORS

Handler `OPTIONS` mengizinkan `Access-Control-Allow-Origin: *`, tetapi helper `json()` hanya mengatur `Cache-Control` dan tidak menambahkan header CORS pada response API normal [11]. Ini membuat integrasi browser lintas origin tidak konsisten dan memperluas akses bila nanti header ditambahkan secara global.

**Rekomendasi:** gunakan allowlist origin untuk web client WAW, tambahkan header CORS secara konsisten pada response yang diperlukan, dan jangan memakai wildcard jika endpoint mengelola data akun.

### Belum ada rate limiting dan abuse protection

Register dan login tidak menunjukkan rate limit, lockout, challenge, audit event, atau throttling per IP/user. PBKDF2 210.000 iterasi sudah lebih baik daripada hash cepat, tetapi endpoint login tetap dapat diserang berulang kali bila tidak dilindungi [11].

**Rekomendasi:** tambahkan rate limiting di edge, exponential backoff, audit log tanpa password/token, batas request body, dan alert untuk pola brute force.

### Validasi input pesan dan ukuran payload belum memadai

WebSocket memvalidasi tipe, conversation ID, dan teks tidak kosong, tetapi tidak menetapkan batas panjang teks, Unicode normalization, quota, atau pembatasan frame. Tidak ada media quota karena media pipeline belum ada [11].

**Rekomendasi:** tetapkan schema validasi bersama client/server, batas ukuran frame, panjang teks, jumlah pesan per menit, dan sanitasi output pada setiap client.

## Build, Test, dan Deployment

CI saat ini memakai Gradle 9.3.1, menjalankan `assembleDebug` dan `testDebugUnitTest`, lalu mengunggah debug APK [10]. Tidak ada release build, signing verification, lint, instrumentation test, backend integration test, two-account test, D1 migration check, atau artifact checksum.

Selain itu, repository tidak menyertakan `gradlew` maupun `gradle-wrapper.properties`; build bergantung pada Gradle yang dipasang runner. Ini melemahkan reproducibility lokal dan dapat menyebabkan hasil berbeda antara environment developer dan CI.

**Gate minimal sebelum rilis:**

| Gate | Kriteria lulus |
|---|---|
| Build | `assembleRelease` berhasil dengan wrapper yang dikunci |
| Static quality | lint, detekt/Ktlint, dan dependency scan lulus |
| Auth | register/login/logout/me diuji pada environment D1 test |
| Chat | dua akun dapat membuat conversation dan bertukar pesan |
| Delivery | ACK sent/delivered/read dan retry diuji |
| Offline | queue, reconnect, duplicate prevention, dan conflict diuji |
| Media | upload, download, permission, quota, dan failed transfer diuji |
| Calls | signaling, permission, ICE failure, hangup, dan background state diuji |
| Story | visibility, expiry, viewer access, deletion, dan privacy diuji |
| Security | token storage, rate limit, CORS, authorization boundary, dan logging diuji |
| Release | signed APK/AAB, checksum, install test, dan upgrade test lulus |

## Rencana Perbaikan Prioritas

### P0 — Hentikan klaim fitur yang belum nyata

Pertahankan label prototype/demo pada UI dan dokumentasi. Jangan menampilkan fake delivery/read status sebagai status server. Sediakan feature flag yang memisahkan demo mode dari production mode.

### P0 — Benahi jalur auth dan chat dasar

Perbaiki query insert, wire `AuthRepository` ke ViewModel, simpan token melalui `SecureStore`, tambahkan bearer header pada WebSocket, buat endpoint conversation creation/list/history, dan implementasikan fan-out atau Durable Object yang sesuai.

### P1 — Persistence dan reliability

Tambahkan local database/queue, cursor-based sync, idempotency, receipt ACK, reconnect dengan backoff dan jitter, conflict handling, serta test dua akun yang berjalan otomatis.

### P1 — Media dan voice note

Bangun permission flow, capture audio, encode/compress, upload resumable ke object storage, metadata attachment, download authorization, thumbnail/transcoding, quota, dan cleanup lifecycle.

### P1/P2 — Call dan story

Untuk call, tentukan signaling service dan WebRTC stack yang sah, lalu uji permission, background/foreground, network switching, NAT/TURN, dan failure states. Untuk story, buat entity, privacy audience, expiry, viewer records, media retention, dan notification policy.

### P2 — Release engineering

Tambahkan Gradle wrapper, release build, signing dari secret CI, lint, dependency audit, instrumentation test, backend integration test, artifact checksum, dan staged rollout. Tambahkan observability yang tidak mengumpulkan isi pesan atau token secara tidak perlu.

## Kesimpulan Akhir

WAW Messenger **belum siap digunakan sebagai WhatsApp replacement atau messenger produksi**. Yang berjalan saat ini adalah shell UI dan prototype infrastructure. Chat, voice note, call, story, media, presence, notifikasi, dan sinkronisasi belum end-to-end. Temuan paling mendesak adalah jalur production masih memakai data demo, WebSocket client tidak mengirim token, query insert pesan memiliki placeholder yang salah, dan backend tidak melakukan fan-out ke penerima.

Arah yang aman adalah memilih salah satu dari dua produk secara eksplisit: **messenger WAW-owned** dengan backend, media, call, dan story milik WAW; atau **authorized linked-device integration** yang hanya menggunakan mekanisme resmi. Kedua jalur tersebut tidak boleh dicampur, dan protokol privat WhatsApp tidak boleh direverse-engineer atau dipalsukan.

## Referensi

[1]: https://github.com/frostbyte-lab/waw-messenger/blob/main/WHATSAPP_1_20_STATUS.md "WAW WhatsApp 1–20 Status"
[2]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/MainActivity.kt "WAW MainActivity"
[3]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/ui/WawChatShell.kt "WAW Chat UI shell"
[4]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/ui/WawChatShell.kt "WAW UI interactions and placeholders"
[5]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/chat/WebSocketChatRepository.kt "WAW WebSocket repository"
[6]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/chat/ChatModels.kt "WAW chat models"
[7]: https://github.com/frostbyte-lab/waw-messenger/blob/main/backend/README.md "WAW backend implementation status"
[8]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/security/SecureStore.kt "WAW Android Keystore store"
[9]: https://github.com/frostbyte-lab/waw-messenger/blob/main/app/src/main/java/com/waw/messenger/auth/AuthRepository.kt "WAW authentication repository"
[10]: https://github.com/frostbyte-lab/waw-messenger/blob/main/.github/workflows/android-build.yml "WAW Android CI workflow"
[11]: https://github.com/frostbyte-lab/waw-messenger/blob/main/backend/worker.js "WAW backend worker"
