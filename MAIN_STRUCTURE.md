# WAW Business Workspace — Struktur Utama

## Konsep wajib

Aplikasi memakai `waw-lengkap-final-targeted.html` sebagai **tampilan utama**. HTML tersebut memiliki alur `onboarding → login → chatlist → workspace`; launcher dibuka pada state `chatlist` sebagai beranda setelah login, dengan tab aktif `Chat` (bukan `Workspace`). Header, daftar obrolan, tab, bottom navigation, kartu, warna, ikon, badge, modal, dan navigasinya tidak diganti dengan tampilan lain.

Aplikasi tidak menampilkan WhatsApp Web. Tidak ada WebView ke `web.whatsapp.com`, tidak ada tampilan login/scan QR WhatsApp, dan tidak ada UI WhatsApp yang ditempel di atas dashboard WAW.

Sistem WhatsApp, bila diaktifkan, hanya berada sebagai kanal/backend resmi di belakang UI WAW. Token, cookie, QR, session, dan credential tidak disimpan di APK.

## Tampilan utama

Halaman pertama adalah **Beranda/Chatlist setelah login**. Isinya mengikuti bagian HTML `LABEL: BERANDA / SETELAH LOGIN`: header WAW BUSINESS, status terhubung, tab Chat/Panggilan/Status/Fitur/Workspace, Workspace Quick Access, daftar obrolan, dan bottom navigation.

Lima kartu berikut berada di **MENU WORKSPACE**, bukan di halaman utama:

1. **Remote Access** — OTP pairing, screen sharing, relay, dan kontrol sentuh.
2. **IP & Lokasi Tracker** — diagnostik IP lokal dan lokasi perangkat dengan izin Android.
3. **Scan PDF & Edit Dokumen** — file manager, editor dokumen, scan/konversi PDF, dan export PDF.
4. **Absensi Fingerprint** — verifikasi biometrik dan pencatatan absensi lokal.
5. **Custom Watermark** — logo, field custom, timestamp, lokasi, kompas, dan export PNG.

## Navigasi fitur

Kartu `Remote Access` membuka modul `RemoteHostActivity`.

Kartu `Scan PDF & Edit Dokumen` membuka alat native `ToolsActivity`/Workspace file manager.

Kartu `Custom Watermark` membuka `WatermarkScreen`.

Kartu `IP & Lokasi Tracker` membuka layar diagnostik lokasi/IP native.

Kartu `Absensi Fingerprint` membuka layar absensi native dan `BiometricGate`.

Jika suatu layar native belum selesai, kartu tetap mengikuti desain HTML tetapi harus menampilkan status yang jelas. Tidak boleh ada data demo, chat palsu, tombol kosong, atau fitur yang terlihat aktif padahal belum terhubung.

## Batas produk

Yang dipertahankan hanya Remote, IP/Lokasi, Scan PDF/Edit Dokumen, Absensi Fingerprint, dan Custom Watermark. Sistem messenger mandiri, UI WhatsApp Web, backup, kalender, notes/tasks, dan modul di luar lima kelompok ini tidak menjadi tampilan utama.

## Aturan implementasi

Perubahan visual harus dimulai dari HTML referensi. Kode native hanya menjadi bridge untuk membuka fungsi perangkat. Setiap tombol dashboard harus terhubung ke fungsi nyata atau diberi status belum tersedia secara eksplisit. Tidak boleh membuat mockup visual yang tidak berasal dari kode aplikasi.
