# Ruang Lingkup WhatsApp Resmi untuk WAW

Dokumentasi resmi Meta menyatakan bahwa WhatsApp Cloud API menyediakan kemampuan utama untuk mengirim pesan dan menerima webhook. Integrasi WAW dapat memakai kanal ini untuk inbox bisnis, pesan masuk/keluar, template, media, dan status pengiriman melalui webhook.

## Dapat digunakan oleh WAW

| Area | Pemakaian di WAW |
|---|---|
| Pesan bisnis | Inbox dan composer bergaya WhatsApp untuk nomor bisnis yang terhubung |
| Webhook | Sinkronisasi pesan masuk, status terkirim, diterima, dibaca, dan event terkait |
| Media | Pengiriman media melalui alur API resmi dengan pemrosesan token di server |
| Template | Pesan bisnis yang memerlukan template sesuai kebijakan Meta |
| Calling API | Fitur panggilan bisnis hanya jika akun dan kemampuan Meta memenuhi syarat |
| Workspace | Lapisan aplikasi milik WAW untuk role, assignment, label, audit, dan inbox tim |

## Tidak dapat dijanjikan dari API resmi

Cloud API bukan API untuk membuat klien WhatsApp pribadi alternatif. WAW tidak boleh menjanjikan akses ke daftar kontak pribadi pengguna, Status WhatsApp pribadi, chat pribadi pengguna, atau seluruh fitur internal aplikasi WhatsApp. Tampilan boleh dibuat menyerupai pola messenger, tetapi data dan kemampuan harus berasal dari akun bisnis serta izin resmi.

## Keputusan desain

Tampilan utama akan memakai pola WhatsApp-style: sidebar percakapan, header kontak, bubble pesan, composer, status koneksi, dan tombol **Workspace**. Tombol Workspace membuka fungsi bisnis seperti assignment, label, role agent, audit, template, dan integrasi kanal. Token Meta hanya berada di backend/secret manager, tidak di APK atau browser.

## Referensi resmi

1. https://developers.facebook.com/documentation/business-messaging/whatsapp/get-started
2. https://developers.facebook.com/documentation/business-messaging/whatsapp/webhooks/overview
3. https://developers.facebook.com/documentation/business-messaging/whatsapp/overview
4. https://developers.facebook.com/documentation/business-messaging/whatsapp/calling
