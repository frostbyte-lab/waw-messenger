# WAW WORKSPACE — CATATAN LENGKAP FINAL (VERSI WEB UJI COBA)

## System UI Tetap, Jalan Kayak WA Pada Umumnya

**URL Live:** https://waw-workspace-review.pages.dev/

**Halaman aktif:** `/data-deletion/` sebagai bukti fitur koneksi, kontak, percakapan, pesan, dan webhook.

**Prinsip:** UI asli WAW dipertahankan 100%, login menggunakan OTP, dan fitur dirancang agar berjalan seperti WhatsApp pada umumnya.

> **Penting:** Versi web ini hanya sistem uji coba, pengecekan, dan preview. Jika seluruh alur berjalan baik di web, versi utama akan diterapkan pada APK dengan tampilan, fitur, dan sistem yang sama.

**Web = Preview dan Testing | APK = Produk Utama**

## 1. Tujuan Versi Web Uji Coba

Versi web digunakan untuk memeriksa login OTP, meninjau detail UI WAW, menguji koneksi, kontak, percakapan, pesan, webhook, panggilan, dan video call, serta melakukan demo kepada tim atau calon pengguna sebelum instalasi APK. Versi web juga digunakan untuk kebutuhan verifikasi domain Meta karena menyediakan URL live.

Versi web bukan produk utama, bukan untuk penggunaan harian skala besar, dan data web hanya digunakan untuk testing. Data utama direncanakan berada pada APK.

Alur pengembangan:

```text
Versi WEB Uji Coba → Check & Preview → OK → Build VERSI APK Utama
```

Semua tampilan, fitur, dan sistem yang disetujui pada web harus diterapkan pada APK utama dengan tampilan dan perilaku yang sama.

## 2. Login — OTP Sederhana

Alur login yang ditargetkan:

```text
Buka WAW Web Uji Coba → Input nomor HP → Kirim OTP → Input OTP 6 digit → Masuk Workspace
```

Tidak menggunakan password, verifikasi email, upload NIB/NPWP, atau proses persetujuan bisnis untuk alur preview.

Teknologi yang direncanakan: Firebase Phone Authentication.

```js
signInWithPhoneNumber(auth, phoneNumber)
confirmationResult.confirm(otp)
```

## 3. UI WAW yang Dipertahankan

Layout utama harus tetap mengikuti sistem UI WAW:

```text
[HEADER] WAW Workspace | Search | Call | VC | Titik Tiga
[SIDEBAR KIRI 30%]              [CHAT KANAN 70%]
- Search Chat                    - Nama Kontak + Status Online
- Filter: Semua, Belum Dibaca    - Bubble Chat seperti WhatsApp
- List Chat                      - Input emoji, file, voice note
                                 - Tombol Call dan Video Call
```

Perubahan UI tidak boleh menghapus branding WAW, layout utama, warna, bubble chat, navigasi, atau fitur workspace yang telah disepakati.

## 4. Fitur yang Harus Diuji

| Fitur | Kebutuhan |
|---|---|
| Koneksi | Status terhubung, menghubungkan, dan offline |
| Kontak | Foto profil, nama, dan nomor HP |
| Percakapan | Bubble kiri/kanan, status terkirim, diterima, dan dibaca |
| Pesan | Teks, gambar, video, file, dan voice note |
| Panggilan | Voice call dan video call |
| Workspace | Grup, status, template, webhook, dan pengaturan |

Fitur yang belum selesai harus ditandai sebagai testing atau roadmap, bukan dianggap sudah tersedia.

## 5. Web Uji Coba dan APK Utama

| Aspek | Web Uji Coba | APK Utama |
|---|---|---|
| Tujuan | Check, preview, dan testing | Produk utama dan pemakaian harian |
| Lokasi | https://waw-workspace-review.pages.dev/ | Play Store atau direct install resmi |
| Login | OTP Firebase | OTP Firebase |
| Tampilan | UI WAW asli | UI WAW asli dan sama |
| Data | Testing atau dummy | Data asli pengguna |
| Koneksi | Browser dan internet | Native, background service, dan push notification |
| Call/VC | WebRTC browser | Native WebRTC yang lebih stabil |
| Status | Sementara | Versi utama |

Prinsip utama: apa yang disetujui dan berjalan di web harus tersedia di APK dengan tampilan dan sistem yang identik, dengan penyesuaian yang memang diwajibkan oleh platform.

## 6. Tahapan Kerja

### Tahap 1 — Web Uji Coba

1. Pengguna membuka https://waw-workspace-review.pages.dev/.
2. Pengguna memasukkan nomor HP dan OTP.
3. Pengguna masuk ke preview workspace.
4. Tim memeriksa UI, chat, koneksi, call, dan video call.
5. Feedback dan perbaikan diterapkan pada web.

### Tahap 2 — APK Utama

1. APK dibangun dengan tampilan dan fitur yang telah disetujui pada web.
2. Pengguna menginstal APK.
3. Pengguna login dengan OTP yang sama.
4. Workspace, chat, kontak, dan riwayat disinkronkan jika backend telah siap.
5. APK menjadi versi utama untuk penggunaan sehari-hari.

## 7. File dan Halaman yang Harus Ada

Pada web uji coba:

- `index.html` untuk login OTP dan tidak boleh 404.
- `/workspace/` untuk UI utama WAW.
- `/data-deletion/` sebagai halaman penghapusan data.
- `/privacy/` untuk kebijakan privasi.
- `/terms/` untuk ketentuan penggunaan.

Pada APK utama:

- Login OTP.
- Workspace.
- Kontak.
- Chat.
- Panggilan dan video call.
- Pengaturan.

## 8. Status Implementasi

Dokumen ini adalah catatan arah produk dan pengujian. Fitur yang belum benar-benar diuji pada URL live atau APK tidak boleh dianggap selesai. Setiap fitur baru wajib melalui implementasi, build, test, dan pemeriksaan keamanan sebelum dinyatakan aktif.

## Kesimpulan

- Versi web digunakan untuk preview dan testing.
- APK menjadi produk utama.
- UI WAW dipertahankan dan tidak boleh diubah tanpa keputusan baru.
- Login ditargetkan menggunakan nomor HP dan OTP.
- Sistem diarahkan untuk menyediakan chat, kontak, koneksi, panggilan, video call, grup, status, template, webhook, dan pengaturan.
- Semua fitur yang disetujui di web harus diterapkan pada APK dengan tampilan dan sistem yang sama.

## Link

- **Web Uji Coba:** https://waw-workspace-review.pages.dev/
- **Data Deletion:** https://waw-workspace-review.pages.dev/data-deletion/
- **Email Penghapusan:** projekmii23@gmail.com
