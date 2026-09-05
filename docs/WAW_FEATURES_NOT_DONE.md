# WAW Messenger — Fitur Belum Selesai

Dokumen ini adalah daftar gap implementasi hasil audit total. Status `DONE` hanya boleh diberikan setelah build, test, pengujian perangkat nyata, pemeriksaan keamanan, dan dokumentasi lulus.

WAW MESSENGER — DAFTAR FITUR YANG BELUM SELESAI
Tanggal audit: 5 September 2026
Repository utama: https://github.com/frostbyte-lab/waw-messenger

Keterangan status:
PARTIAL = sudah ada fondasi/UI, tetapi alur produksi belum lengkap.
NOT STARTED = belum ada implementasi fungsional yang dapat diverifikasi.
PENDING = memerlukan pengujian, konfigurasi, perangkat, atau keputusan release.
BLOCKED = belum dapat diselesaikan sebelum dependensi atau jalur resmi tersedia.

============================================================
PRIORITAS 1 — AKUN, PROFIL, CHAT, DAN NETWORK RECOVERY
============================================================

[PARTIAL] Login production
- Tambahkan session persistence yang aman.
- Tambahkan expiry dan refresh/revocation yang diuji.
- Tambahkan rate limiting dan perlindungan brute force.
- Tambahkan integration test dan test perangkat nyata.
- Tambahkan observability untuk login gagal/berhasil tanpa mencatat password/token.

[PARTIAL] Register production
- Tambahkan verifikasi email atau mekanisme verifikasi akun.
- Tambahkan perlindungan abuse/rate limit.
- Tambahkan aturan username/email duplicate yang konsisten.
- Tambahkan test failure case dan rollback database.

[IMPLEMENTED PARTIAL] Logout
- Pastikan token lokal, cache user, WebSocket, draft, dan data akun dibersihkan sesuai kebijakan.
- Uji logout dari banyak perangkat.
- Uji session yang sudah dicabut tidak dapat digunakan kembali.

[PARTIAL] Pemulihan password
- Endpoint forgot-password sudah ditambahkan.
- Endpoint reset-password sudah ditambahkan.
- Token reset disimpan sebagai hash, sekali pakai, dengan expiry 15 menit.
- Session aktif dicabut setelah password berhasil diubah.
- Respons forgot-password bersifat generik untuk mencegah account enumeration.
- Delivery email masih memerlukan konfigurasi server-side `RESET_EMAIL_WEBHOOK`.
- Layar input token/password baru belum selesai.
- Rate limit khusus dan audit event reset masih perlu ditambahkan.

[NOT STARTED] Pergantian akun
- Account switcher.
- Penyimpanan beberapa session secara aman.
- Pemisahan cache, draft, database lokal, dan notification per akun.
- Logout satu akun tanpa menghapus akun lain.
- Penghapusan session tersimpan.

[PARTIAL] Validasi form dan pesan error
- Error schema API yang konsisten.
- Error per field.
- Pesan offline, timeout, server, credential, dan rate-limit.
- Accessibility untuk error dan fokus input.
- Retry yang tidak menggandakan register/login.

[NOT STARTED] Profil dan pengaturan
- Halaman profil.
- Upload dan ganti foto profil.
- Edit nama dan email.
- Ganti password.
- Tema terang/gelap yang dipersistenkan.
- Bahasa.
- Privasi.
- Pengaturan akun.
- Pengaturan notifikasi.

[PARTIAL] Sinkronisasi chat lintas perangkat
- Initial history load pada aplikasi Android.
- Pagination/cursor.
- Deduplication berdasarkan client_id/idempotency key.
- Reconciliation setelah reconnect.
- Server-authoritative ordering.
- Conflict handling.
- Cache lokal yang konsisten.

[PARTIAL] Real-time inbound message
- Broadcast event kepada anggota conversation yang sedang online.
- Presence/online state.
- Reconnect subscription.
- Event versioning.
- Fan-out yang aman dan teruji.

[PARTIAL] Status pesan
- Status SENT sudah ada secara dasar.
- Status DELIVERED belum selesai.
- Status READ belum selesai.
- markRead() Android masih kosong.
- Endpoint receipt dan database receipt belum lengkap.
- Sinkronisasi status lintas perangkat belum ada.

[NOT STARTED] Outbox dan offline queue
- Durable local outbox.
- Queue pesan ketika offline.
- Retry manual.
- Exponential backoff dengan batas.
- Idempotency agar pesan tidak terduplikasi.
- Rekonsiliasi queue setelah reconnect.
- NetworkSyncWorker masih hook dan belum menjalankan sync nyata.

[NOT STARTED] Draft pesan
- Draft per conversation.
- Persistence lokal.
- Sinkronisasi draft bila dibutuhkan.
- Hapus draft setelah berhasil terkirim.

[NOT STARTED] Attachment
- Upload gambar.
- Upload video.
- Upload dokumen.
- Object storage.
- Upload progress dan retry.
- Thumbnail/preview.
- MIME/type/size validation.
- Attachment message schema.
- Retention dan delete policy.

[NOT STARTED] Voice note
- Permission microphone.
- Recorder.
- Pause/resume/cancel.
- Encoding dan upload.
- Playback.
- Waveform/duration.
- Retry dan retention.

============================================================
PRIORITAS 2 — KOMUNIKASI
============================================================

[NOT STARTED] Voice call
- Signaling server.
- WebRTC peer connection.
- STUN/TURN.
- Incoming call screen.
- Mute.
- Speaker/headset.
- Call state machine.
- Permission microphone.
- Call history.
- Reconnect/network failure handling.

[NOT STARTED] Video call
- Permission camera.
- Local/remote video track.
- Camera enable/disable.
- Call screen.
- Background/foreground handling.
- TURN fallback.
- Call history and failure states.

[NOT STARTED] Status/update
- Status teks.
- Upload foto/video.
- Expiry otomatis.
- Daftar penonton.
- Balasan status.
- Privacy status.
- Delete status.
- Status sync lintas perangkat.

[NOT STARTED] Search
- Search chat.
- Search pesan.
- Search kontak.
- Search file.
- Filter tanggal.
- Filter tipe.
- Filter pengirim.
- Pagination/index.
- Empty/error/loading state.

[NOT STARTED] Push notification
- FCM token registration.
- Server-side delivery.
- Notification channel.
- Badge count.
- Deep link conversation.
- Incoming call notification.
- Suara/getar setting.
- Token rotation dan revoke.
- Privacy-safe notification preview.

============================================================
PRIORITAS 3 — WORKSPACE
============================================================

[PARTIAL] Workspace foundation
- Sebagian UI shell tersedia.
- Hubungkan semua action ke modul nyata.
- Hilangkan angka/list hardcoded.
- Tambahkan server sync.
- Tambahkan workspace membership dan collaboration.
- Tambahkan role owner/admin/member.
- Tambahkan audit log.

[PARTIAL] File manager
- Validasi device nyata.
- Folder favorit.
- Recent files.
- Batch action.
- Sort/filter lengkap.
- Storage usage nyata.
- MIME preview yang lebih luas.
- Permission/storage edge cases.
- Konflik rename/copy/move.

[PARTIAL] Dokumen dan PDF
- Viewer PDF production.
- Anotasi PDF.
- Merge PDF.
- Split PDF.
- Reorder/rotate halaman.
- Scan kamera ke PDF.
- Crop/deskew/enhancement.
- Watermark.
- Export/share lanjutan.
- Test file besar dan file rusak.

[NOT STARTED] Tugas
- Tambah tugas.
- Deadline.
- Prioritas.
- Assignee.
- Checklist.
- Komentar.
- Status tugas.
- Reminder.
- Sinkronisasi workspace.
- Activity log.

[NOT STARTED] Kalender
- Tambah agenda.
- Meeting.
- Reminder.
- Undangan peserta.
- Agenda berulang.
- Sinkronisasi kalender.
- Timezone handling.
- Conflict handling.

============================================================
PRIORITAS 4 — KEAMANAN DAN PRIVASI
============================================================

[PARTIAL] Biometric/App Lock
- Timeout otomatis.
- Lock ketika background.
- Fallback PIN/device credential.
- Lock per dokumen atau fitur sensitif.
- Re-authentication setelah timeout.
- Test device dengan dan tanpa biometric enrollment.

[PARTIAL] Secure Vault
- UI vault lengkap.
- Import file.
- Kategori.
- Search.
- Lock per item.
- Encrypted file container.
- Secure deletion policy.
- Encrypted backup.
- Recovery policy tanpa menyimpan biometric data.

[NOT STARTED] WAW Shield
- Pemeriksaan link nyata.
- Blocklist domain.
- Reputation service.
- Warning page.
- Redirect analysis.
- Histori pemeriksaan.
- Laporan domain.
- Privacy-preserving lookup.
- Status UNKNOWN jika bukti tidak cukup.

[PARTIAL] Backend security
- Rate limiting.
- Abuse detection.
- Password reset security.
- Email verification.
- Production CORS policy.
- Structured audit logs.
- Secret rotation.
- Data retention/deletion.
- Account export/delete.
- Backup and restore test.

============================================================
PRIORITAS 5 — RELEASE PUBLIK
============================================================

[PARTIAL] Android release
- Signed APK hanya berjalan jika secret signing lengkap.
- Unsigned APK tidak boleh dianggap release publik.
- Keystore production harus dibuat dan disimpan melalui secret manager.
- Apksigner verification harus lulus.
- Versioning dan changelog harus konsisten.

[NOT STARTED] Android App Bundle
- Build `bundleRelease`.
- Signed `.aab`.
- Verify signature.
- Upload artifact.
- Test install dari internal testing.

[PENDING] Pengujian perangkat nyata
- Login/register/logout.
- Offline/reconnect.
- Chat multi-device.
- Biometric.
- File/PDF.
- Notification.
- Background/foreground.
- Permission denial.
- Low storage.
- Network switching Wi-Fi/mobile.

[NOT STARTED] Play Store preparation
- Play Console listing.
- Privacy policy.
- Data safety form.
- Content declaration.
- Permission disclosure.
- Screenshots/icon/feature graphic.
- Internal testing.
- Play App Signing.
- Crash monitoring.
- Rollback plan.

============================================================
BLOCKER KONTRAK INTEGRASI
============================================================

[BLOCKED BY OFFICIAL ACCESS] Integrasi WhatsApp privat
- Jangan menyalin database WhatsApp.
- Jangan mengambil token/cookie/session secret.
- Jangan reverse-engineer atau replay protokol privat.
- Jangan menyimpan credential pihak lain di APK, frontend, log, atau repository.
- Gunakan akun, backend, chat, media, status, call, dan workspace milik WAW sendiri.
- Integrasi pihak ketiga hanya melalui jalur resmi dan terdokumentasi.

============================================================
URUTAN PENGERJAAN YANG DISARANKAN
============================================================

1. Auth production: forgot/reset password, account switcher, session persistence, rate limit.
2. Chat core: server history, broadcast, outbox, retry, idempotency, receipts, reconnect sync.
3. Media dan notification: attachments, voice note, FCM, badge, deep link.
4. Profile/settings: avatar, edit profile, theme, language, privacy, notification settings.
5. Workspace: file manager validation, PDF, tasks, calendar, collaboration.
6. Security: app lock, vault, WAW Shield, privacy/data lifecycle.
7. Calls/status/search: WebRTC/TURN, status lifecycle, search index.
8. Release: signed AAB, device matrix, Play Console, monitoring, rollback.

DEFINITION OF DONE

Fitur hanya boleh diberi status DONE jika implementasi selesai, build berhasil, unit/integration test lulus, workflow utama berhasil pada perangkat nyata, failure case diuji, security check lulus, dokumentasi diperbarui, dan tidak ada blocker yang diketahui.
