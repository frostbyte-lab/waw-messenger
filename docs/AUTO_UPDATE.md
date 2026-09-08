# Auto-update APK WAW

## Cara kerja

Setiap push ke branch `main` menjalankan workflow `.github/workflows/android-release.yml`. Workflow tersebut:

1. Menghasilkan versi `0.2.<nomor-run>` dan `versionCode` yang selalu meningkat.
2. Membuild APK release.
3. Menandatangani APK dengan production keystore yang sama.
4. Membuat GitHub Release bertag `v<versi>`.
5. Mengunggah asset dengan nama tetap `waw-release.apk`.

Aplikasi memeriksa GitHub Releases API saat dibuka. Jika ditemukan release stabil dengan versi lebih tinggi dan asset `waw-release.apk`, pengguna mendapat dialog update. Setelah pengguna menyetujui, APK diunduh dan installer Android dibuka.

Android tidak mengizinkan aplikasi biasa memasang APK secara diam-diam. Karena itu, update otomatis di sini berarti **pemeriksaan otomatis, pengunduhan, lalu konfirmasi pemasangan oleh Android**.

## Secret yang wajib tersedia

Tambahkan empat secret berikut pada GitHub Actions environment `production`:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Keystore harus sama dengan yang dipakai untuk APK WAW yang sudah terpasang di perangkat. Jika berbeda, Android menolak update karena signature tidak cocok. Jangan membuat keystore baru untuk setiap release; simpan backup keystore secara aman di luar GitHub.

## Catatan rilis

Workflow akan gagal dengan sengaja jika secret signing belum tersedia. Ini mencegah APK test-signed terpublikasi dan kemudian tidak dapat memperbarui APK production. Pull request tetap menggunakan workflow build/test terpisah dan tidak menerbitkan release.
