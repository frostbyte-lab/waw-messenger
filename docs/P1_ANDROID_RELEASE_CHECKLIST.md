# P1 Android Release Checklist

## Sudah dikerjakan

- Gradle Wrapper `9.3.1` tersedia di repository.
- Unit test Kotlin tersedia untuk retry policy, local chat behavior, dan security boundary.
- `release` build type tersedia dengan aturan ProGuard minimal.
- Workflow manual `WAW Android Release Artifact` tersedia untuk menghasilkan APK release unsigned.
- Signing key tidak disimpan di repository.

## Masih memerlukan tindakan pemilik proyek

| Item | Status | Catatan |
|---|---|---|
| Keystore release | PENDING | Harus dibuat dan disimpan di GitHub Secrets atau sistem signing terpisah. |
| APK signed | PENDING | Tidak boleh dilakukan dengan debug key. |
| Validasi perangkat nyata | PENDING | Memerlukan perangkat Android yang dapat diakses penguji. |
| Smoke test biometric/app lock | PENDING | Harus diuji pada device dengan biometric enrollment. |
| Play App Signing / Play Console | PENDING | Memerlukan akun dan kredensial pemilik proyek. |
| Test matrix | PENDING | Minimal mencakup API 26 sampai target SDK, network loss, dan fresh install/upgrade. |

## Batas keamanan

Workflow release saat ini hanya menghasilkan **APK unsigned sebagai artifact**. Ia tidak mengunggah ke Play Store dan tidak membuat signing key. Keputusan ini disengaja agar credential signing tidak masuk ke source control atau tercipta tanpa persetujuan pemilik proyek.
