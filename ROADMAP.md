# WAW — MASTER ROADMAP

## TARGET UTAMA

WAW ditujukan sebagai aplikasi Android yang mengikuti **model WhatsApp asli**, dengan fokus utama pada konsep **linked/companion device** terhadap akun WhatsApp yang sah.

WAW **bukan** messenger mandiri dengan akun WAW + backend chat sendiri sebagai jalur utama.

Target arsitektur:

```text
WhatsApp Primary Phone
        │
        │  Link Device / Companion Device
        │  QR / metode linking resmi
        ▼
      WAW Android
        │
        │  sesi perangkat tertaut
        ▼
 WhatsApp account/session
        │
        ├── Chat
        ├── Contacts
        ├── Groups
        ├── Media
        ├── Notifications
        ├── Calls
        └── Device management
```

### ATURAN PALING PENTING

1. WAW harus mengikuti **mekanisme resmi yang tersedia** untuk linked/companion device.
2. Jangan membuat server WAW sebagai pengganti jaringan WhatsApp.
3. Jangan meminta password WhatsApp pengguna.
4. Jangan mengambil, menyalin, atau membundel credential, token, cookie, private key, atau session rahasia WhatsApp.
5. Jangan bypass, spoof, replay, atau melemahkan kontrol keamanan WhatsApp.
6. Jangan reverse-engineer protokol privat untuk menghindari mekanisme resmi.
7. Bila suatu kemampuan tidak tersedia melalui mekanisme resmi, fitur tersebut diberi status `BLOCKED / NOT_SUPPORTED`, bukan dipaksa melalui bypass.
8. Semua pengujian menggunakan akun dan perangkat yang memang berhak digunakan oleh penguji.
9. Jangan menganggap fitur selesai hanya karena UI sudah tampil.
10. Setiap tahap wajib: **IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK → NEXT**.

---

# 18. OLD STANDALONE CHAT CODE

Kode lama yang menggunakan WAW username/password, WAW sessions, WAW conversations/messages, WAW WebSocket, dan Cloudflare D1 sebagai chat authority diperlakukan sebagai **legacy/prototype architecture**, bukan target utama produk.

## Audit

- [x] Audit `AuthRepository`
- [x] Audit `ChatRepository`
- [x] Audit `WebSocketChatRepository`
- [x] Audit D1 chat schema/backend
- [x] Tentukan kode legacy yang masih berguna sebagai referensi/UI seam
- [x] Jangan hapus sebelum pengganti tervalidasi
- [x] Pisahkan legacy dari production path
- [x] Production `MainActivity` tidak lagi memakai repository chat standalone
- [x] Tidak ada penghapusan massal

## Validation Gate

- [ ] Android build berhasil
- [ ] Unit tests berhasil
- [ ] APK debug berhasil dibuat
- [ ] Instalasi dan smoke test pada emulator/perangkat nyata
- [ ] App lock/biometric smoke test
- [ ] Legacy code tetap tidak menjadi production data source

**STATUS: 🟡 IMPLEMENTED — NOT LOCKED**

Phase 18 hanya boleh menjadi **LOCKED** setelah build/test dan validasi perangkat benar-benar lulus.

---

## NEXT GATE

Setelah Phase 18 lulus, lanjut ke audit/implementasi mekanisme **official linked/companion-device feasibility** sebelum membangun chat production. Jika mekanisme resmi tidak menyediakan kemampuan tertentu untuk aplikasi target, tandai `BLOCKED / NOT_SUPPORTED`.
