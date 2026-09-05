# Arsitektur Awal WAW

## Lapisan Aplikasi

```text
Presentation
  ├── App shell dan tab navigation
  ├── Workspace grid dan feature detail screens
  └── Permission, progress, empty, dan error states

Feature modules
  ├── scanner
  ├── document-converter
  ├── sharing
  ├── printing
  ├── biometrics
  ├── media-player
  ├── location
  ├── remote
  └── screen-sharing

Platform adapters
  ├── Camera / media picker
  ├── File system
  ├── Android Share Sheet
  ├── Android Print Framework
  ├── Biometric prompt
  └── Media playback

Storage
  ├── Local workspace files
  ├── User preferences
  └── Secure pairing/session metadata
```

## Aturan Modul

Setiap modul memiliki layar masuk, status kemampuan perangkat, penanganan izin, aksi utama, dan jalur pembatalan. Modul tidak boleh langsung bergantung pada layar tab; komunikasi antarmodul dilakukan melalui service atau kontrak tipe yang jelas.

## Berbagi ke WhatsApp

Alur yang diharapkan adalah `hasil file → validasi URI dan MIME type → Android Share Sheet → pengguna memilih WhatsApp resmi → pengguna memilih chat dan mengirim`. WAW hanya menyerahkan file kepada sistem operasi. WAW tidak membaca daftar chat, isi pesan, kontak internal, atau status pengiriman WhatsApp.

## Remote Access

Remote access harus menggunakan pairing sekali pakai, token berumur pendek, TLS, pencabutan sesi, daftar perangkat tepercaya, dan indikator koneksi yang selalu terlihat. Kontrol mouse, keyboard, dan layar hanya boleh aktif setelah persetujuan pada kedua endpoint. Implementasi produksi memerlukan threat model sebelum protokol dipilih.

## Penyimpanan dan Privasi

File kerja disimpan lokal secara default. Metadata sensitif seperti token pairing tidak disimpan dalam plain text. Izin kamera, mikrofon, lokasi, notifikasi, aksesibilitas, dan screen capture diminta hanya ketika fitur yang bersangkutan digunakan.
