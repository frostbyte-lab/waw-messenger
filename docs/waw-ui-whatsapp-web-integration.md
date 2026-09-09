# Integrasi UI WAW dengan WhatsApp Web Resmi

## Keputusan arsitektur

WAW menggunakan tampilan dan navigasi milik WAW, sedangkan fungsi chat, login, sinkronisasi, media, dan sesi WhatsApp dijalankan oleh **WhatsApp Web resmi** pada `https://web.whatsapp.com`. WAW tidak menggantikan backend WhatsApp, tidak membaca credential internal, dan tidak mengimpor cookie atau token WhatsApp ke layanan WAW.

## Struktur tampilan

```text
WAW UI Shell
├── Logo dan branding WAW
├── Header WAW Business
├── Tab Chat, Panggilan, Status, Fitur, Workspace
├── Status koneksi resmi
└── Area konten WhatsApp Web resmi
    └── https://web.whatsapp.com
```

Pada halaman review `https://waw-workspace-review.pages.dev/`, teks dan visual landing page dapat dijadikan referensi desain WAW. Namun, halaman tersebut saat ini masih menyatakan bahwa sistemnya hanya mockup. Implementasi produksi harus memisahkan landing/dashboard WAW dari area resmi WhatsApp Web dan tidak boleh membuat layar login WhatsApp palsu.

## Implementasi yang sudah ada di Android

`LinkedDeviceWebViewActivity` memuat URL resmi `https://web.whatsapp.com`. Host navigasi dibatasi ke `web.whatsapp.com` melalui `isAllowedWhatsAppNavigation`. Permintaan kamera dan mikrofon hanya diberikan apabila origin-nya adalah host resmi dan User telah memberikan izin Android. WebView tidak mengekspor cookie, session secret, QR credential, atau token WhatsApp ke WAW.

Workspace WAW dibuka sebagai modul milik WAW melalui tab **Workspace**. Dengan demikian, fitur seperti Remote Workspace, File Manager, dan Document Tools tetap merupakan fitur WAW, bukan fitur yang menyamar sebagai kemampuan internal WhatsApp.

## Aturan UI

| Area | Sumber UI | Sumber fungsi |
|---|---|---|
| Branding, logo, header, tab, status wrapper | WAW | WAW |
| Chat dan daftar percakapan | WhatsApp Web resmi | WhatsApp Web resmi |
| Login/QR linking | WhatsApp Web resmi | Mekanisme resmi WhatsApp |
| Panggilan dan media | WhatsApp Web resmi jika tersedia | WhatsApp Web resmi |
| Remote Workspace | WAW | Backend relay WAW dengan persetujuan eksplisit |
| File Manager dan dokumen WAW | WAW | Backend/local storage WAW |
| Credential WhatsApp | Tidak boleh ditampilkan atau disalin oleh WAW | WhatsApp resmi |

## Acceptance criteria

1. Saat User membuka Chat, URL yang dimuat adalah `https://web.whatsapp.com`.
2. QR login dan layar linking berasal dari WhatsApp Web resmi.
3. WAW tidak meminta password WhatsApp.
4. WAW tidak menyimpan atau mengirim cookie, token, QR secret, atau private key WhatsApp ke relay WAW.
5. Navigasi eksternal yang bukan `web.whatsapp.com` ditolak atau diproses sebagai link eksternal yang aman.
6. Workspace Remote memiliki status dan consent sendiri serta tidak mengklaim sebagai fitur resmi WhatsApp.
7. Landing page dapat memakai desain WAW, tetapi tidak boleh menyatakan bahwa mockup sudah terhubung sebelum koneksi nyata diuji.
8. Jika kemampuan WhatsApp resmi tidak tersedia, UI menampilkan `BLOCKED / NOT_SUPPORTED` dan tidak menggunakan protokol privat sebagai jalan pintas.

## Status

Implementasi Android saat ini sudah memuat WhatsApp Web resmi dan menyediakan UI shell WAW. Halaman Cloudflare Pages masih berupa review/mockup dan belum menjadi sumber runtime chat. Tahap berikutnya untuk web production adalah menghubungkan shell WAW yang sama ke alur resmi WhatsApp Web tanpa menyalin credential atau membuat backend WhatsApp alternatif.
