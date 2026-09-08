# Workspace-only migration

Repository ini sekarang difokuskan pada fitur milik WAW dan tidak lagi menyertakan sistem messenger/WhatsApp Web. Kode yang dihapus dari aplikasi meliputi launcher messenger, layar login, splash messenger, updater APK lama, dan linked WebView.

Launcher aplikasi sekarang adalah `com.waw.messenger.WorkspaceActivity`. Kelompok fitur yang dipertahankan di source adalah Remote PC/Android, File & Dokumen, Watermark, Scan/komponen PDF, Absensi Fingerprint, serta diagnostik IP dan lokasi perangkat.

Workspace Hub menampilkan fitur yang sudah memiliki layar aktif sebagai kartu yang dapat dibuka: File & Dokumen, Watermark, dan Remote. Scan PDF, Absensi Fingerprint, IP & Lokasi, serta Convert Image to PDF ditampilkan sebagai komponen yang tersedia di source tetapi belum memiliki layar UI terhubung; kartu-kartu tersebut sengaja nonaktif agar tidak ada tombol palsu.

Tag pemulihan sebelum migrasi adalah `pre-wa-removal-20260908`. Tag tersebut hanya digunakan sebagai titik pemulihan Git dan tidak menjadi bagian dari aplikasi aktif.
