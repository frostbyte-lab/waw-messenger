# WAW Remote Control

WAW Remote Control mendukung dua target: **Android → Windows** dan **Android → Android**. Sesi remote wajib memakai kode pairing sekali pakai, persetujuan pada perangkat host, dan tombol revoke/putuskan.

## Status implementasi

Android memiliki layar Remote Control, pembuat pairing code lokal, pemilihan target, dan permintaan izin sistem MediaProjection. Relay sekarang memaksa dua persetujuan: izin screen share dari User dan approval eksplisit dari Operator. Koneksi realtime, agent Windows, serta input injection masih memerlukan validasi perangkat dan hardening produksi.

## Batas keamanan

Remote tidak boleh berjalan tersembunyi. Host harus melihat indikator screen share, menyetujui sesi melalui dialog sistem, Operator harus menekan Approve, dan kedua pihak dapat mencabut sesi kapan saja. Relay mencatat event pairing, approval, dan revoke tanpa menyimpan password, cookie WhatsApp, atau token Workspace.

## Komponen lanjutan

Agent Windows dan transport realtime perlu dibuat sebagai komponen terpisah dengan TLS, autentikasi mutual, expiry token, rate limit, dan audit log lokal. Android-to-Android memakai alur izin yang sama pada kedua perangkat.
