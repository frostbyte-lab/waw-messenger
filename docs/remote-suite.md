# WAW Remote Suite

WAW Remote Suite dibagi menjadi tiga komponen dengan peran yang tegas:

| Komponen | Platform | Peran |
| --- | --- | --- |
| **WAW Remote User** | Android | Perangkat target. Membuat OTP, memberikan izin MediaProjection, menampilkan indikator screen share, dan dapat mencabut akses. |
| **WAW Control Center** | Windows | Konsol admin desktop. Memasukkan OTP, menerima preview layar, dan mengirim kontrol dasar. |
| **WAW Control Mobile** | Android | Konsol admin mobile. Memasukkan OTP, menerima preview layar, dan mengirim kontrol dasar. |

## Alur sesi

1. Instal **WAW Remote User** pada PC/Android yang akan dikendalikan dan buka menu Remote.
2. User membuat OTP enam digit. OTP disimpan melalui `SecureStore` dan berlaku selama dua menit.
3. Admin memasukkan OTP pada WAW Control Center atau WAW Control Mobile bersama URL relay `wss://`.
4. User menekan **Izinkan & mulai remote** dan menyetujui dialog MediaProjection Android.
5. Setelah izin sistem diberikan, User mengirim pesan approval ke relay. Relay baru meneruskan frame dan perintah setelah approval tersebut.
6. Sesi aktif memiliki batas waktu konfigurasi, default delapan jam, dan dapat diakhiri oleh User maupun Admin.

## Keamanan minimum

Pairing memakai OTP sekali pakai dan relay wajib memakai TLS melalui `wss://`. OTP bukan pengganti persetujuan sistem: screen share tetap memerlukan dialog izin Android. Relay menolak frame dan input sebelum approval, memvalidasi `sessionId`, membatasi satu viewer per OTP, serta menghapus sesi saat salah satu peer terputus.

## Menjalankan komponen desktop

```powershell
cd remote/control-center
npm install
npm start
```

Aplikasi desktop memakai Electron dan WebSocket. Untuk produksi, relay harus dipasang di belakang reverse proxy TLS dan tidak boleh diekspos sebagai `ws://` publik.

## Status implementasi

Fondasi WAW Remote User berasal dari modul Android utama. WAW Control Mobile tersedia sebagai modul `:admin-android`. WAW Control Center tersedia di `remote/control-center` dengan dashboard desktop dark glassmorphism. Kontrol input penuh pada Android target masih memerlukan implementasi `AccessibilityService` yang disetujui User; screen sharing dan alur pairing adalah bagian yang tersedia pada fondasi ini.
