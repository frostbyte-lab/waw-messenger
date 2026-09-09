# Batas Integrasi WhatsApp Resmi

Tanggal audit: 2026-09-09.

WhatsApp menyatakan bahwa linked devices dapat digunakan untuk mengakses WhatsApp pada Web, Windows, Mac, tablet Android, iPad, dan perangkat lain yang didukung. Akun utama dapat menautkan hingga empat perangkat, dan perangkat tertaut tetap bekerja ketika ponsel utama tidak sedang online. WhatsApp juga memperingatkan bahwa aplikasi atau situs tidak resmi untuk menautkan akun dapat membahayakan akun dan berpotensi menyebabkan pemblokiran. Karena itu WAW harus memuat `https://web.whatsapp.com` secara langsung dan tidak mengirimkan QR, cookie, token, atau pesan WhatsApp ke backend WAW.

WhatsApp mengumumkan pada 28 Juli 2026 bahwa panggilan audio dan video tersedia langsung dari WhatsApp Web, termasuk panggilan satu lawan satu dan grup, screen sharing, reactions, tab Calls, serta riwayat panggilan; ketersediaannya dilakukan bertahap. Implementasi WAW harus menampilkan tombol Calls sebagai navigasi ke WhatsApp Web, bukan membangun klien panggilan WhatsApp alternatif.

Status/story juga harus tetap dikelola oleh WhatsApp Web di dalam sesi resmi. WAW dapat menyediakan tab dan chrome visual miliknya sendiri, tetapi tidak boleh menyimulasikan status, mengkloning kontak, atau memproses data WhatsApp secara terpisah jika tujuan pengguna adalah komunikasi WhatsApp resmi.

## Konsekuensi implementasi

| Fitur | Implementasi aman di WAW |
|---|---|
| Chat | WebView resmi menuju `https://web.whatsapp.com`; UI WAW hanya chrome/navigasi yang tidak mengambil alih pesan. |
| Voice/video call | Tombol Calls membuka tab/fungsi Calls di WhatsApp Web; izin kamera/mikrofon hanya diberikan kepada origin resmi `web.whatsapp.com`. |
| Story/status | Tombol Status membuka fungsi Status di WhatsApp Web; tidak ada status palsu atau sinkronisasi duplikat. |
| Link device | QR/linking dilakukan oleh WhatsApp Web dan ponsel utama pengguna. |
| Backend WAW | Tidak menerima cookie, QR, isi chat, token sesi, atau kredensial WhatsApp. |
| Workspace WAW | Fitur lokal seperti catatan, dokumen, tugas, dan kalender tetap terpisah dari data WhatsApp. |

## Referensi

[1]: https://faq.whatsapp.com/378279804439436 About linked devices — WhatsApp Help Center.
[2]: https://faq.whatsapp.com/1317564962315842 How to link a device — WhatsApp Help Center.
[3]: https://blog.whatsapp.com/introducing-web-calling-on-whatsapp-plus-more-new-updates Introducing Web Calling on WhatsApp — WhatsApp Blog, 28 July 2026.

