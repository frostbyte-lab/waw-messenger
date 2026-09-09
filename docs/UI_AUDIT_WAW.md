# Audit Tampilan dan UX WAW

## Kesimpulan

Audit menunjukkan bahwa **alur utama WAW sudah konsisten dengan keputusan desain**: WAW tetap menjadi tampilan utama, sedangkan proses linking dijalankan oleh WhatsApp Web resmi. Halaman Cloudflare Pages juga sudah tidak menampilkan form email/password. Satu masalah UX pada APK ditemukan dan diperbaiki selama audit, yaitu tombol `Login Google WAW` yang hanya menampilkan pesan bahwa fitur belum dikonfigurasi.

## Ruang lingkup audit

Audit mencakup layar login Android, shell WebView WAW, navigasi menuju WhatsApp Web resmi, teks credential, permission kamera/mikrofon, serta halaman Cloudflare Pages publik.

## Hasil pemeriksaan

| Area | Hasil | Status |
|---|---|---|
| Branding WAW pada layar login Android | Logo, warna hijau gelap, kartu keamanan, dan tombol linking konsisten | Lulus |
| Form email/password WhatsApp | Tidak digunakan pada layar login Android | Lulus |
| Tombol Google yang belum aktif | Ditemukan dan dihapus | Diperbaiki |
| Tombol utama | Membuka `LinkedDeviceWebViewActivity` di dalam APK | Lulus |
| Sumber linking | `https://web.whatsapp.com` | Lulus |
| Shell WAW pada WebView | Header, tab, status, dan Workspace tetap tersedia | Lulus |
| QR/linking saat halaman dimuat | Shell WAW tetap dibuat visible setelah halaman selesai dimuat | Lulus secara kode |
| Permission kamera/mikrofon | Hanya diberikan untuk origin resmi WhatsApp | Lulus |
| Navigasi eksternal | Host dibatasi ke `web.whatsapp.com` dan WAW Shield | Lulus |
| Cloudflare Pages | Tombol resmi tersedia dan teks mockup lama hilang | Lulus |
| Responsivitas | Android memakai scrolling vertikal pada layar login | Lulus secara layout |
| Aksesibilitas dasar | Tombol memiliki teks terlihat dan ikon pendamping | Lulus sebagian |

## Temuan yang sudah diperbaiki

### Tombol mati pada layar login Android

Sebelumnya layar Android masih mempunyai tombol `Login Google WAW`. Tombol tersebut tidak melakukan autentikasi dan hanya menampilkan toast `belum dikonfigurasi`. Elemen ini membingungkan pengguna serta tidak sesuai dengan alur linking resmi. Tombol dan callback-nya sudah dihapus.

### Konsistensi alur login

Layar login Android sekarang hanya memiliki satu tindakan utama, yaitu `Hubungkan WhatsApp`. Teks pendukung menjelaskan bahwa QR/linking resmi ditampilkan di dalam WAW dan WAW tidak meminta password WhatsApp.

## Catatan visual

Shell WebView memakai header putih dengan logo WAW, label `WAW BUSINESS`, tab Chat, Panggilan, Status, Fitur, dan Workspace, serta indikator `WHATSAPP WEB RESMI • Linking di dalam WAW`. Hierarki visual sudah jelas, tetapi lima tab dalam satu baris dapat menjadi padat pada perangkat dengan lebar layar kecil. Pengujian perangkat nyata pada lebar 320–360 dp tetap direkomendasikan sebelum rilis produksi.

## Batasan teknis yang harus dipertahankan

WhatsApp Web tidak boleh disalin menjadi backend atau login buatan WAW. WebView hanya boleh memuat origin resmi, tidak boleh mengirim cookie atau session WhatsApp ke relay, dan tidak boleh meminta password WhatsApp. Pada Cloudflare Pages, browser dapat menolak iframe WhatsApp Web; karena itu tombol web tetap boleh melakukan navigasi ke halaman resmi, sedangkan APK Android menggunakan WebView internal.

## Rekomendasi tahap berikutnya

Pengujian visual berikutnya sebaiknya dilakukan pada perangkat Android kecil, sedang, dan besar. Pengujian perlu mencakup kondisi QR belum dipindai, linking berhasil, permission kamera ditolak, koneksi lambat, orientasi layar berubah, dan tombol Back ditekan. Jika tab terlalu padat pada perangkat kecil, solusi yang disarankan adalah membuat tab dapat digeser horizontal tanpa mengubah identitas visual WAW.

## Status akhir

Audit tampilan dan UX selesai. Temuan tombol Google tidak aktif sudah diperbaiki. Build Android perlu dijalankan ulang sebagai validasi akhir terhadap perubahan tersebut sebelum APK baru dibagikan.

## References

[1]: https://web.whatsapp.com "WhatsApp Web resmi"
[2]: https://waw-workspace-review.pages.dev/ "WAW Workspace Review live page"
[3]: https://github.com/frostbyte-lab/waw-messenger "WAW Messenger repository"
