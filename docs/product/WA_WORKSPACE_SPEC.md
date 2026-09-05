# CATATAN — WA.W Messenger

## Status proyek

Repository ini sengaja dimulai sebagai **repository kosong berisi catatan proyek**. Belum ada APK, backend, database, atau credential yang dimasukkan. Implementasi kode baru dimulai setelah ruang lingkup dan keputusan arsitektur disetujui.

## Tujuan produk

WA.W Messenger adalah aplikasi messenger mandiri untuk Android yang memiliki pengalaman penggunaan yang familiar seperti aplikasi pesan modern, dengan tambahan fitur Workspace untuk tim dan bisnis. Aplikasi ini memiliki sistem akun, kontak, percakapan, media, status sementara, panggilan, dan ruang kerja bisnis milik WA.W sendiri.

Aplikasi ini **bukan klien WhatsApp pribadi**, bukan GBWhatsApp, dan tidak akan membongkar atau memakai API internal WhatsApp. Integrasi ke WhatsApp asli hanya dilakukan melalui WhatsApp Business Platform/Cloud API resmi Meta dan bersifat opsional untuk kebutuhan bisnis.

## Ruang lingkup fitur

| Modul | Fitur yang direncanakan |
|---|---|
| Akun | Registrasi, login, profil, foto profil, pemulihan akun, dan pengaturan privasi |
| Kontak | Meminta izin Android `READ_CONTACTS`, menampilkan kontak yang dipilih pengguna, pencarian, dan undangan pengguna WA.W |
| Chat | Pesan teks real-time, reply, forward, hapus, reaksi, pesan suara, dan indikator terkirim/terbaca |
| Media | Foto, video, dokumen, kamera, kompresi, preview, dan penyimpanan object storage |
| Panggilan | Voice call dan video call menggunakan WebRTC dengan signaling server serta TURN server |
| Status | Membuat status foto/video/teks, melihat status kontak WA.W, masa aktif 24 jam, dan penghapusan otomatis |
| Grup | Membuat grup, anggota, admin grup, nama/foto grup, mute, dan keluar grup |
| Workspace | Workspace pribadi/bisnis, role owner/admin/agent, inbox tim, assignment percakapan, label, catatan internal, dan audit log |
| Integrasi Meta | Pengiriman dan penerimaan pesan bisnis melalui Cloud API resmi, template, media, dan webhook |
| Notifikasi | Push notification untuk pesan, mention, assignment, dan panggilan masuk |

## Batasan integrasi WhatsApp resmi

API resmi Meta tidak memberikan akses umum ke kontak pribadi, Status pribadi, chat pribadi, atau seluruh fungsi aplikasi WhatsApp. Karena itu, kontak dan status pada aplikasi WA.W adalah data milik platform WA.W sendiri.

WhatsApp Business Platform digunakan hanya untuk kanal bisnis. Pengiriman harus mengikuti kebijakan opt-in, template pesan, customer-service window, quality rating, messaging limits, dan ketentuan lain dari Meta. Token Meta tidak boleh disimpan di APK, frontend, `localStorage`, log, atau repository.

## Arsitektur yang direncanakan

| Lapisan | Rencana |
|---|---|
| Aplikasi Android | React Native/Expo dengan modul native untuk kontak, kamera, media, push notification, dan WebRTC |
| Backend API | Node.js/TypeScript dengan API autentikasi, chat, status, Workspace, dan integrasi Meta |
| Real-time | WebSocket atau Socket.IO untuk pesan dan signaling panggilan |
| Database | PostgreSQL/Supabase untuk akun, relasi kontak, pesan, status metadata, Workspace, role, dan audit log |
| Media storage | Object storage S3-compatible untuk foto, video, dokumen, dan avatar |
| Call relay | STUN/TURN server untuk koneksi WebRTC pada jaringan yang sulit |
| Push | FCM untuk Android dan APNs bila kelak mendukung iOS |
| Integrasi Meta | Graph API resmi dan webhook terverifikasi |

## Keamanan dan privasi

Aplikasi harus menggunakan HTTPS, password hashing, session yang aman, validasi input, rate limiting, kontrol akses berbasis Workspace, isolasi tenant, enkripsi credential Meta, validasi signature webhook, audit log, backup, dan mekanisme penghapusan akun serta data.

Akses kontak harus diminta secara jelas dan hanya digunakan untuk fungsi yang disetujui pengguna. Aplikasi tidak boleh mengirim pesan otomatis kepada seluruh kontak tanpa tindakan dan persetujuan yang jelas dari pengguna.

## Tahapan implementasi

| Tahap | Hasil yang diharapkan |
|---|---|
| 1 | Spesifikasi dan keputusan arsitektur disetujui |
| 2 | Project Android dan backend dasar tersedia |
| 3 | Akun, kontak, chat, media, dan status 24 jam berfungsi |
| 4 | Voice/video call WebRTC dan push notification berfungsi |
| 5 | Workspace, role, inbox tim, label, assignment, dan audit log berfungsi |
| 6 | Integrasi Cloud API dan webhook resmi Meta diuji |
| 7 | Signing APK/AAB, pengujian, privacy policy, dan persiapan Google Play |

## Keputusan yang perlu dikonfirmasi

| Keputusan | Nilai awal |
|---|---|
| Platform awal | Android APK |
| Backend | Belum dipilih |
| Database | Belum dipilih; PostgreSQL/Supabase direkomendasikan untuk awal |
| Hosting | Belum dipilih |
| Login | Belum dipilih; email/password dapat dipakai untuk prototipe |
| Nama aplikasi | WA.W Messenger, dapat diubah sebelum release |
| Integrasi Meta | Opsional, hanya melalui jalur resmi |

## Credential

Tidak ada credential nyata di repository ini. Credential yang kelak dibutuhkan harus dimasukkan melalui secret manager atau environment server. Jangan menyimpan access token Meta, App Secret, password database, signing keystore, atau private key di GitHub.

## Referensi

[1]: [Meta Developers — About the WhatsApp Business Platform](https://developers.facebook.com/documentation/business-messaging/whatsapp/about-the-platform)
[2]: [Meta Developers — Cloud API Calling](https://developers.facebook.com/documentation/business-messaging/whatsapp/calling)
[3]: [WhatsApp Business — Messaging Policy](https://whatsappbusiness.com/policy/)

## Materi produk enterprise

**Nama produk:** WA.W — WhatsApp Workspace  
**Developer:** FrostByte Tech. Ltd  
**Harga yang tercantum pada materi:** Rp450.000 per tahun  
**Distribusi yang direncanakan:** di luar Play Store, melalui jalur resmi Meta atau distribusi terkontrol lainnya. Status verifikasi Meta, harga final, dan metode distribusi masih harus dibuktikan sebelum dipublikasikan.

Materi promosi menyebut sepuluh kelompok kemampuan berikut sebagai target enterprise:

| No. | Fitur | Rencana implementasi | Status verifikasi |
|---:|---|---|---|
| 1 | Remote Access PC | Agent desktop yang hanya aktif setelah persetujuan eksplisit, dengan device pairing, command allowlist, audit log, dan tombol putus darurat | Belum diverifikasi |
| 2 | Fingerprint Authentication | Biometric prompt Android untuk membuka sesi lokal; tidak menggantikan autentikasi server | Belum diimplementasikan |
| 3 | CamScanner Pro | Kamera, crop/deskew, OCR opsional, konversi PDF, dan watermark | Belum diimplementasikan |
| 4 | Barcode/QR Scanner | Pemindaian untuk absensi, inventaris, atau verifikasi internal Workspace | Belum diimplementasikan |
| 5 | Custom Watermark | Watermark diterapkan di server atau pipeline media sebelum file dibagikan | Belum diimplementasikan |
| 6 | Integrasi resmi Meta | WhatsApp Cloud API resmi, webhook terverifikasi, template, dan status pesan; tidak menjamin bebas banned | Klaim resmi harus diverifikasi |
| 7 | Real-time Location | Berbagi lokasi berbasis opt-in, indikator aktif, batas waktu, penghentian manual, dan kebijakan retensi | Belum diimplementasikan |
| 8 | Anti-judol/anti-spam | Filter konten berbasis aturan dan/atau model, mekanisme false-positive review, serta perlindungan privasi | Belum diimplementasikan |
| 9 | Enkripsi enterprise | Gunakan protokol dan library yang dapat diaudit; nama atau klaim “Gortex 1.1.3” belum memiliki bukti teknis | Belum diverifikasi |
| 10 | Engine AI | Auto-reply, rangkuman, dan laporan dengan provider yang dipilih; data sensitif harus memiliki kontrol pemrosesan | Provider dan desain belum ditentukan |

## Rekomendasi penggunaan enterprise

| Peran | Fitur utama |
|---|---|
| Admin/CS | Integrasi kanal resmi Meta, filter spam, inbox Workspace, template, dan bantuan AI |
| Manager/Owner | Workspace, audit log, watermark, remote access yang disetujui, dan lokasi berbasis opt-in |
| Operasional | Scanner dokumen, PDF, barcode/QR, assignment, dan label |

## Action item verifikasi

Sebelum materi dipakai untuk promosi atau penjualan, pemilik produk perlu memverifikasi keaslian onboarding dan API Meta, menyediakan demo Remote Access dan Location Tracking, memastikan izin serta audit keamanannya, mengonfirmasi harga Rp450.000/tahun, menyediakan informasi trial, dan menjelaskan metode distribusi APK.

Klaim **“Anti-Banned System”** tidak boleh dipasarkan sebagai jaminan. Penggunaan API resmi dapat mengurangi risiko teknis dari API tidak resmi, tetapi akun tetap tunduk pada kebijakan Meta, kualitas pesan, opt-in, template, limit, dan kemungkinan pembatasan atau penghentian akses.

## Catatan distribusi

Jika aplikasi tidak didistribusikan melalui Play Store, distribusi APK harus menjelaskan sumber unduhan, identitas penerbit, checksum, privacy policy, mekanisme update, dan peringatan keamanan. Untuk distribusi Play Store pada masa depan, aplikasi harus mengikuti persyaratan signing, Data safety, permission disclosure, dan kebijakan Google Play.
