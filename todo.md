
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

- [ ] Kontrak protokol: sessionId, approval, expiry, revoke, capability, dan ukuran pesan
- [ ] Relay: token sesi, rate limit pairing, validasi role, dan audit event
- [ ] MediaProjection: frame transport stabil dengan backpressure dan lifecycle aman
- [ ] Foreground service: notification ongoing dan action revoke
- [ ] Accessibility: tap, swipe, text input, dan approved action allowlist
- [ ] Transfer file: allowlist, batas ukuran, consent, dan audit
- [ ] Admin–User: pairing, approval, preview, command, file, dan revoke end-to-end
- [ ] User standalone: service dan manifest terhubung, bukan hanya layar consent
- [ ] Test relay/protocol serta build Android jika SDK tersedia

## Perbaikan lanjutan Remote Access

- [ ] Audit ulang gap implementasi pada branch main
- [ ] Hapus bug compile dan ketidaksesuaian status approval
- [ ] Lengkapi relay session/token/rate-limit/revoke
- [ ] Lengkapi MediaProjection dan frame transport
- [ ] Lengkapi Accessibility tap/swipe/text/approved actions
- [ ] Lengkapi file transfer dengan consent dan checksum
- [ ] Lengkapi User standalone dan integrasi Admin
- [ ] Jalankan test relay dan build Android; perbaiki temuan
- [ ] Push hasil perbaikan dan laporan ke GitHub
