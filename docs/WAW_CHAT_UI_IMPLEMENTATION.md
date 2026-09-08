# WAW Chat UI — Implementasi

## Ringkasan

WAW sekarang menyediakan lapisan antarmuka native di atas WhatsApp Web resmi. Pengguna tetap terhubung melalui `https://web.whatsapp.com`, sedangkan header, tab navigasi, status koneksi, composer, dan kontrol aksi disediakan oleh WAW.

## Fitur yang ditambahkan

- Header WAW dengan tab Chat, Panggilan, Status, Fitur, dan Workspace.
- Composer pesan native bergaya WAW dengan kolom input, emoji, tombol kirim, panggilan suara, dan panggilan video.
- Status composer: terhubung, sedang mengetik, dan terkirim.
- Bridge pengiriman pesan terbatas ke elemen `contenteditable` WhatsApp Web.
- Bridge tombol panggilan yang mencari tombol resmi WhatsApp berdasarkan label aksesibilitas.
- Tab Fitur membuka kontrol screen share WAW berbasis MediaProjection dan OTP pairing.
- Activity dan foreground service screen share didaftarkan di manifest dengan permission Android resmi.
- Session WhatsApp, cookie, token, dan credential tidak diekspor ke WAW.

## Batasan teknis yang disengaja

WhatsApp Web tidak menyediakan API publik untuk mengambil atau mengganti seluruh DOM chat secara stabil. Struktur internal dan label tombol dapat berubah tanpa pemberitahuan. Karena itu bridge hanya melakukan aksi yang dipicu pengguna dan gagal dengan aman jika elemen target tidak tersedia.

Panggilan suara/video tetap menggunakan media pipeline resmi WhatsApp Web. Kontrol screen share memakai dialog persetujuan MediaProjection Android dan tidak berjalan diam-diam. Implementasi saat ini adalah **screen sharing remote**, bukan perekaman file video lokal; hal ini mencegah perekaman tanpa persetujuan eksplisit.

## Cara uji manual

1. Pasang APK release terbaru dan buka koneksi WhatsApp resmi.
2. Setelah WhatsApp Web menampilkan chat, pastikan header WAW dan composer muncul.
3. Fokus kolom input dan pastikan status berubah menjadi `SEDANG MENGETIK`.
4. Kirim pesan percobaan dan verifikasi pesan tampil di chat resmi.
5. Uji tombol panggilan dari chat yang aktif.
6. Buka tab `Fitur`, buat OTP, isi relay `wss://`, lalu setujui dialog screen share Android.
7. Putuskan sesi melalui tombol `Putuskan`.

## Keamanan

Jangan menambahkan selector untuk mengekstrak QR, cookie, session secret, daftar kontak, atau isi chat ke luar WebView. Semua koneksi harus tetap dibatasi ke host resmi WhatsApp dan aksi sensitif harus dimulai dari persetujuan pengguna.

## Bugfix login/scan

Pada layar login atau QR scan, seluruh chrome WAW kini disembunyikan: header, tab, bottom navigation, dan composer chat tidak tampil. UI WAW baru ditampilkan setelah WhatsApp Web tidak lagi memiliki indikator login/scan. Deteksi dilakukan berulang selama halaman aktif dan di-reset setiap navigasi halaman agar tidak ada tombol yang menumpuk di atas QR.
