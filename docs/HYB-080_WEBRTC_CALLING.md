# HYB-080 — WebRTC dan Calling

## Internal WAW calling

Panggilan internal WAW menggunakan WebRTC dengan signaling yang tenant/workspace-scoped. Call session menyimpan state, participant authorization melalui conversation membership, media yang diminta, active indicator, dan emergency hangup. Signaling payload tidak boleh membawa credential layanan atau data di luar call.

## Consent

Kamera, mikrofon, dan screen share memerlukan consent eksplisit dari participant. Consent dapat ditolak atau dicabut. Aplikasi harus menampilkan indikator aktif selama media capture berlangsung. Emergency hangup mengakhiri call dan menghentikan capture lokal serta signaling session.

## Network

ICE configuration, STUN, dan TURN harus berasal dari server-side secret/configuration. Credential TURN berumur pendek. APK tidak menyimpan token Cloudflare atau secret permanen.

## WhatsApp Business Calling

Voice calling WhatsApp Business bukan bagian dari internal WebRTC session. Fitur tersebut hanya dapat diaktifkan jika nomor bisnis dan account resmi Meta memenuhi eligibility Calling API. Jalur adapter, webhook, audit, dan delivery event harus tetap berlabel `whatsapp_business`.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Isolation | User di luar tenant/workspace tidak dapat join atau signal call |
| Consent | Camera, microphone, dan screen share memiliki grant/reject/revoke state |
| Indicator | Participant melihat active media indicator |
| Emergency | Emergency hangup menghentikan media dan mengakhiri session |
| Signaling | Offer, answer, ICE, dan hangup tervalidasi serta tidak menyimpan secret |
| Network | STUN/TURN config server-side; credential ephemeral |
| Failure | Denied permission, reconnect, timeout, dan failed state diuji |
| Separation | WhatsApp Calling hanya melalui jalur resmi Business API dan label berbeda |
