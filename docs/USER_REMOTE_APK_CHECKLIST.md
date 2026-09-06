# Checklist APK User Remote Workspace

## Tujuan dan batas produk

APK User Remote Workspace adalah aplikasi Companion mandiri yang dapat dibagikan melalui link dan dipasang tanpa APK WAW utama. Aplikasi ini menjadi perangkat target yang dikendalikan oleh Operator/Admin setelah User memberikan persetujuan eksplisit. APK Admin tidak boleh dibundel atau dibagikan bersama APK User.

> **Kriteria selesai utama:** remote hanya boleh aktif setelah User menyetujui izin yang terlihat, Operator menyetujui sesi, dan sistem menampilkan indikator aktif yang tidak dapat disembunyikan. User harus dapat menekan **Revoke** untuk memutus sesi secara langsung.

## Status saat ini

| Komponen | Status | Catatan |
|---|---:|---|
| Modul Gradle `user-remote` | Selesai awal | APK standalone sudah terdaftar sebagai modul terpisah. |
| Nama aplikasi | Selesai awal | `User Remote Workspace`. |
| Logo awal | Selesai awal | Logo gelap-oranye tersedia dan dapat diganti per workspace. |
| Layar persetujuan granular | Selesai awal | Screen, input, file, dan tindakan terkontrol ditampilkan sebagai pilihan. |
| Build debug APK | Lulus | APK debug berhasil dikompilasi. |
| Pairing OTP ke relay | Belum selesai | UI dan transport standalone belum terhubung. |
| Persetujuan Operator | Belum selesai | Relay sudah mendukung approval, tetapi APK standalone belum memakai client tersebut. |
| Screen streaming | Belum selesai | MediaProjection perlu dihubungkan ke transport standalone. |
| Input tap dan keyboard | Belum selesai | Accessibility service perlu diintegrasikan dan dibatasi oleh capability. |
| Transfer file | Belum selesai | Protokol, UI picker, batas ukuran, dan audit belum tersedia di APK standalone. |
| Approved actions | Belum selesai | Harus menggunakan allowlist, bukan shell arbitrer. |
| Revoke real-time | Belum selesai | Tombol UI perlu benar-benar memutus service, socket, token, dan projection. |
| Cloudflare Tunnel | Belum aktif | `cloudflared` belum tersedia dan tunnel publik belum dikonfigurasi. |
| Distribusi GitHub Release | Disiapkan | Workflow GitHub sudah diarahkan untuk menerbitkan APK User. |
| Signing produksi | Belum diverifikasi | Keystore produksi dan SHA-256 harus tersedia sebelum distribusi luas. |

## Pekerjaan aplikasi User

### 1. Identitas workspace dan konfigurasi undangan

- [ ] Menerima `workspaceId`, nama workspace, logo, warna brand, dan alamat relay dari invite token.
- [ ] Menolak konfigurasi yang tidak memiliki `workspaceId` atau signature invite yang valid.
- [ ] Menampilkan nama workspace pada header, layar persetujuan, notifikasi, dan layar sesi aktif.
- [ ] Menyimpan konfigurasi non-rahasia secara lokal.
- [ ] Menyimpan token sesi menggunakan Android Keystore atau penyimpanan terenkripsi.
- [ ] Menetapkan masa berlaku invite token dan menolak token yang kedaluwarsa.
- [ ] Menyediakan fallback jika logo workspace gagal dimuat.
- [ ] Tidak mengizinkan invite mengubah application ID, signature APK, atau permission sistem.

### 2. Onboarding dan persetujuan User

- [x] Menampilkan tutorial singkat tentang remote session.
- [x] Menampilkan izin secara granular.
- [x] Meminta User mencentang bagian akses yang disetujui.
- [ ] Menampilkan identitas Operator, nama workspace, waktu mulai, masa berlaku, dan tujuan sesi.
- [ ] Menampilkan peringatan bahwa screen, input, file, dan actions adalah capability yang berbeda.
- [ ] Meminta persetujuan tambahan untuk transfer file.
- [ ] Meminta persetujuan tambahan untuk tindakan berisiko.
- [ ] Menyimpan bukti persetujuan berupa capability, timestamp, session ID, dan versi aplikasi.
- [ ] Menolak memulai sesi jika salah satu capability yang diwajibkan belum disetujui.
- [ ] Menyediakan halaman untuk melihat dan mencabut capability sebelum sesi aktif.

### 3. Pairing dan token sesi

- [ ] Membuat pairing code acak menggunakan `SecureRandom`.
- [ ] Membatasi pairing code menjadi sekali pakai.
- [ ] Memberi TTL pendek untuk pairing code.
- [ ] Mengikat pairing ke workspace, device ID non-sensitif, dan session ID.
- [ ] Mengirim pairing request hanya melalui TLS/WSS.
- [ ] Menolak replay token.
- [ ] Menolak lebih dari satu Operator untuk satu pairing kecuali mode multi-operator secara eksplisit diaktifkan.
- [ ] Memisahkan pairing token dari session token.
- [ ] Menghapus token saat sesi selesai atau saat User menekan Revoke.
- [ ] Menampilkan status `WAITING_FOR_OPERATOR`, `APPROVED`, `ACTIVE`, `REVOKED`, `EXPIRED`, dan `ERROR`.

### 4. Screen sharing

- [ ] Meminta izin MediaProjection melalui dialog sistem Android.
- [ ] Tidak mengambil frame sebelum User menyetujui MediaProjection.
- [ ] Menjalankan screen capture hanya dalam foreground service.
- [ ] Menampilkan notifikasi permanen selama screen capture aktif.
- [ ] Mengirim frame melalui koneksi terenkripsi.
- [ ] Membatasi frame rate dan ukuran frame untuk mencegah beban berlebihan.
- [ ] Menghapus frame dari memory setelah dikirim.
- [ ] Tidak menyimpan screenshot secara permanen tanpa persetujuan terpisah.
- [ ] Menghentikan MediaProjection ketika socket tertutup, token kedaluwarsa, service dihentikan, atau User menekan Revoke.
- [ ] Menangani rotasi layar, perubahan resolusi, dan screen lock.

### 5. Input tap, swipe, dan keyboard

- [ ] Menjelaskan fungsi Accessibility Service sebelum mengarahkan User ke Settings.
- [ ] Meminta User mengaktifkan Accessibility Service secara manual.
- [ ] Memastikan service tidak aktif untuk tujuan lain di luar sesi remote.
- [ ] Memeriksa capability `TOUCH_INPUT` dan `KEYBOARD_INPUT` sebelum menjalankan command.
- [ ] Memvalidasi koordinat, jenis input, durasi, dan urutan event di sisi User.
- [ ] Menolak command yang tidak memiliki session ID yang benar.
- [ ] Menolak command dari Operator yang tidak disetujui.
- [ ] Menghentikan input injection saat Revoke diterima.
- [ ] Tidak memberikan akses root, shell bebas, atau bypass permission.

### 6. Transfer file

- [ ] Menggunakan Android Storage Access Framework atau picker eksplisit.
- [ ] Menampilkan arah transfer: upload atau download.
- [ ] Menampilkan nama, tipe, ukuran, dan hash file sebelum transfer.
- [ ] Meminta konfirmasi User untuk setiap transfer atau batch yang disetujui.
- [ ] Membatasi ukuran dan jumlah file.
- [ ] Menggunakan stream terenkripsi dengan checksum.
- [ ] Menyimpan file hanya pada lokasi yang dipilih User.
- [ ] Menghapus file sementara setelah sesi berakhir atau transfer gagal.
- [ ] Mencatat audit file tanpa menyimpan isi file ke log.

### 7. Approved actions

- [ ] Mendefinisikan allowlist tindakan yang didukung.
- [ ] Menyimpan schema setiap tindakan: nama, parameter, risiko, dan capability.
- [ ] Menolak perintah arbitrer atau shell bebas.
- [ ] Meminta konfirmasi tambahan untuk tindakan destruktif atau sensitif.
- [ ] Menampilkan deskripsi tindakan kepada User sebelum eksekusi.
- [ ] Memberikan timeout dan idempotency key untuk setiap tindakan.
- [ ] Mencatat hasil sukses, gagal, dibatalkan, dan timeout.
- [ ] Menghentikan queue tindakan ketika Revoke ditekan.

### 8. Status, notifikasi, dan Revoke

- [ ] Menampilkan indikator `REMOTE ACTIVE` yang selalu terlihat.
- [ ] Menampilkan nama workspace dan Operator pada notifikasi.
- [ ] Menyediakan tombol **Revoke Access** dari layar utama aplikasi.
- [ ] Menyediakan tombol **Revoke** dari notifikasi foreground service.
- [ ] Memutus WebSocket saat Revoke.
- [ ] Mengirim event revoke ke relay jika koneksi masih tersedia.
- [ ] Menghentikan MediaProjection.
- [ ] Menghentikan Accessibility input session.
- [ ] Membatalkan transfer file dan action queue.
- [ ] Menghapus token dan capability aktif.
- [ ] Menampilkan waktu dan alasan sesi dihentikan.
- [ ] Memastikan sesi tidak dapat aktif kembali tanpa persetujuan baru.

### 9. Audit dan keamanan lokal

- [ ] Menyimpan audit log minimal: session ID, workspace, Operator, capability, timestamp, dan event.
- [ ] Tidak mencatat OTP, session token, file content, screen frame, atau credential ke log biasa.
- [ ] Mengenkripsi data sensitif menggunakan Android Keystore.
- [ ] Menolak cleartext traffic.
- [ ] Memvalidasi sertifikat atau menggunakan konfigurasi TLS yang aman pada relay.
- [ ] Membatasi ukuran pesan masuk dan keluar.
- [ ] Menerapkan rate limit pada input, frame, file, dan actions.
- [ ] Menghapus data sesi ketika aplikasi di-uninstall atau User memilih reset workspace.
- [ ] Menonaktifkan backup untuk token dan material sensitif.
- [ ] Melakukan threat model sebelum rilis produksi.

## Pekerjaan relay dan Admin

- [ ] Menghubungkan APK User standalone ke relay WebSocket yang sama dengan Admin.
- [ ] Memastikan alur `pair-request → user-consent → operator-approval → active`.
- [ ] Menolak frame dan input sebelum approval kedua pihak.
- [ ] Memeriksa session ID, token, role, capability, dan expiry pada setiap pesan.
- [ ] Menerapkan batas ukuran payload dan rate limit per sesi.
- [ ] Menutup sesi ketika salah satu peer disconnect.
- [ ] Menulis audit event terstruktur.
- [ ] Menambahkan health check relay.
- [ ] Menyiapkan TLS/WSS publik.
- [ ] Menjalankan relay sebagai service persisten, bukan proses sandbox sementara.
- [ ] Menguji reconnect tanpa menghidupkan kembali capability secara otomatis.

## Cloudflare Tunnel dan GitHub

Cloudflare Tunnel tidak aktif pada sandbox saat dokumen ini dibuat. `cloudflared` belum tersedia dan connector Cloudflare API/Tunnel belum diaktifkan. R2 tidak diperlukan untuk distribusi APK. GitHub Releases cukup digunakan untuk menyimpan dan membagikan APK.

- [ ] Menentukan domain relay publik.
- [ ] Menentukan apakah relay berjalan pada host persisten atau layanan WebSocket yang dikelola.
- [ ] Memasang dan mengautentikasi `cloudflared` pada host relay jika tunnel dipilih.
- [ ] Membuat route hostname ke service relay lokal.
- [ ] Memastikan endpoint publik hanya `wss://`.
- [ ] Menguji koneksi dari jaringan seluler dan Wi-Fi berbeda.
- [ ] Menyimpan token tunnel sebagai secret, bukan di repository.
- [ ] Membuat GitHub Release untuk APK User.
- [ ] Menyertakan SHA-256 APK.
- [ ] Tidak mengunggah APK Admin ke release User.
- [ ] Menandatangani APK dengan keystore produksi.
- [ ] Menguji instalasi APK pada perangkat Android bersih.

## Pengujian penerimaan

| Skenario | Hasil yang diwajibkan |
|---|---|
| User membuka APK pertama kali | Tutorial dan daftar capability tampil. Tidak ada koneksi remote otomatis. |
| User tidak mencentang semua izin | Tombol lanjut tetap nonaktif. |
| User menekan setuju | Pairing dibuat dan status menunggu Operator tampil. |
| Operator belum approve | Tidak ada frame, input, file, atau action yang diproses. |
| Operator approve | Sesi aktif setelah User dan Operator sama-sama menyetujui. |
| User menekan Revoke | Screen capture, input, transfer, dan action berhenti segera. |
| Token kedaluwarsa | Sesi ditutup dan pairing baru diperlukan. |
| Relay terputus | Aplikasi masuk state aman dan tidak menyambung kembali dengan capability lama. |
| APK dipasang tanpa WAW utama | APK User tetap dapat berjalan mandiri sesuai desain. |
| APK Admin dibagikan ke perangkat lain | APK Admin tidak menjadi artefak User dan tidak memperoleh akses tanpa WAW/signature yang sesuai. |
| File berukuran besar | Transfer ditolak atau dibatasi sesuai policy tanpa crash. |
| Operator mengirim command ilegal | Command ditolak dan dicatat sebagai security event. |

## Definition of Done APK User

APK User Remote Workspace dapat disebut selesai hanya jika build release berhasil, APK ditandatangani, relay publik WSS tersedia, pairing dan approval dua pihak telah diuji, screen sharing berjalan, input yang diizinkan berjalan, transfer file memiliki konfirmasi, approved actions memakai allowlist, Revoke menghentikan semua capability, audit log tersedia, dan pengujian dilakukan pada minimal satu perangkat Android nyata.

## Referensi

[1]: https://developer.android.com/media/grow/media-projection "Android MediaProjection documentation"

[2]: https://developer.android.com/develop/ui/views/touch-and-input/gestures "Android input and gesture documentation"

[3]: https://developer.android.com/guide/topics/ui/accessibility/service "Android AccessibilityService documentation"

[4]: https://developer.android.com/training/secure-file-sharing "Android secure file sharing documentation"

[5]: https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/ "Cloudflare network connections documentation"

[6]: https://docs.github.com/en/repositories/releasing-projects-on-github "GitHub Releases documentation"
