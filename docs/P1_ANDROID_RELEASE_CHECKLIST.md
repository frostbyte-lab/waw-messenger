# P1 Android Release Checklist

## Sudah dikerjakan

- Gradle Wrapper `9.3.1` tersedia di repository.
- Unit test Kotlin tersedia untuk retry policy, local chat behavior, dan security boundary.
- `release` build type tersedia dengan aturan ProGuard minimal.
- Workflow manual `WAW Android Release Artifact` tersedia untuk menghasilkan APK release unsigned.
- Signing key tidak disimpan di repository.
- Workflow release mendukung signing otomatis bila empat secret berikut tersedia: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, dan `ANDROID_KEY_PASSWORD`.

## Masih memerlukan tindakan pemilik proyek

| Item | Status | Catatan |
|---|---|---|
| Keystore release | PENDING | Harus dibuat lalu disimpan sebagai Base64 pada secret `ANDROID_KEYSTORE_BASE64`; jangan commit file ke repository. |
| Password/alias signing | PENDING | Tambahkan `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, dan `ANDROID_KEY_PASSWORD` pada environment `production`. |
| APK signed | OTOMATIS JIKA SECRET LENGKAP | Workflow akan menghasilkan artifact signed dan menjalankan `apksigner verify`; jika secret belum lengkap, workflow hanya menghasilkan artifact unsigned. |
| Validasi perangkat nyata | PENDING | Memerlukan perangkat Android yang dapat diakses penguji. |
| Smoke test biometric/app lock | PENDING | Harus diuji pada device dengan biometric enrollment. |
| Play App Signing / Play Console | PENDING | Memerlukan akun dan kredensial pemilik proyek. |
| Test matrix | PENDING | Minimal mencakup API 26 sampai target SDK, network loss, dan fresh install/upgrade. |

## Batas keamanan

Workflow release menghasilkan **APK signed** bila empat secret signing tersedia; jika belum, workflow menghasilkan **APK unsigned** sebagai fallback. Workflow tidak mengunggah ke Play Store dan tidak membuat signing key. Keputusan ini disengaja agar credential signing tidak masuk ke source control atau tercipta tanpa persetujuan pemilik proyek.
