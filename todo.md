
## Audit repository GitHub

- [x] Audit struktur modul dan branch aktif
- [x] Audit Remote Access item 1 sampai 9
- [x] Audit pairing, consent, revoke, relay, dan foreground service
- [x] Jalankan test relay dan pemeriksaan Gradle/build (test relay lulus; Gradle terblokir karena Android SDK tidak tersedia)
- [x] Catat temuan dan prioritas perbaikan dalam laporan audit

## Audit dengan perbaikan langsung

- [x] Setiap bug nyata yang ditemukan dicatat, diperbaiki, dan diuji ulang (Admin approve)
- [x] Jangan menandai fitur Remote Access sebagai selesai sebelum implementasi dan validasinya nyata

## Remote Access — Perbaikan menyeluruh

- [x] Kontrak protokol: sessionId, approval, expiry, revoke, capability, dan ukuran pesan (dengan validasi relay)
- [x] Relay: token sesi, rate limit pairing, validasi role, dan audit event (rate limit/capability/session ID diperbaiki)
- [x] MediaProjection: frame transport stabil dengan backpressure dan lifecycle aman (row padding/backpressure diperbaiki)
- [x] Foreground service: notification ongoing dan action revoke (tersedia; uji perangkat masih terbuka)
- [x] Accessibility: tap, swipe, text input, dan approved action allowlist (swipe nyata ditambahkan)
- [x] Transfer file: allowlist, batas ukuran, consent, dan audit (implementasi ada; hardening lanjutan tetap diperlukan)
- [x] Admin–User: pairing, approval, preview, command, file, dan revoke end-to-end (relay test lulus)
- [x] User standalone: service dan manifest terhubung, bukan hanya layar consent
- [ ] Test relay/protocol serta build Android jika SDK tersedia

## Perbaikan lanjutan Remote Access

- [x] Audit ulang gap implementasi pada branch main
- [x] Hapus bug compile dan ketidaksesuaian status approval
- [x] Lengkapi relay session/token/rate-limit/revoke (fondasi; token persistence/device binding masih terbuka)
- [x] Lengkapi MediaProjection dan frame transport
- [x] Lengkapi Accessibility tap/swipe/text/approved actions
- [x] Lengkapi file transfer dengan consent dan checksum
- [x] Lengkapi User standalone dan integrasi Admin
- [ ] Jalankan test relay dan build Android; perbaiki temuan
- [x] Push hasil perbaikan dan laporan ke GitHub

## Redesign WAW bergaya WhatsApp resmi

- [x] Ubah shell utama menjadi layout WhatsApp-style dengan branding WAW
- [x] Tambahkan tombol Workspace sebagai fitur tambahan utama
- [x] Pertahankan inbox bisnis melalui WhatsApp Cloud API resmi Meta (alur resmi tetap melalui LinkedDeviceWebViewActivity)
- [x] Jangan menampilkan token Meta di APK/frontend
- [x] Bedakan fitur API Meta dari fitur WhatsApp pribadi yang tidak tersedia
- [ ] Uji tampilan mobile/desktop dan integrasi tanpa credential nyata
