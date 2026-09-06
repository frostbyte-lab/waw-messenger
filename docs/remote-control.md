# WAW Remote Control

WAW Remote Control mendukung dua target: **Android → Windows** dan **Android → Android**. Sesi remote wajib memakai kode pairing sekali pakai, persetujuan pada perangkat host, dan tombol revoke/putuskan.

## Status implementasi

Android memiliki layar Remote Control, pembuat pairing code lokal, pemilihan target, dan permintaan izin sistem MediaProjection. Ini adalah fondasi host dan persetujuan; koneksi realtime, agent Windows, serta input injection belum diaktifkan.

## Batas keamanan

Remote tidak boleh berjalan tersembunyi. Host harus melihat indikator screen share, menyetujui sesi melalui dialog sistem, dan dapat mencabut pairing kapan saja. Password, cookie WhatsApp, dan token Workspace tidak pernah dipakai sebagai kredensial remote.

## Komponen lanjutan

Agent Windows dan transport realtime perlu dibuat sebagai komponen terpisah dengan TLS, autentikasi mutual, expiry token, rate limit, dan audit log lokal. Android-to-Android memakai alur izin yang sama pada kedua perangkat.
