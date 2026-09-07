# WAW Release Readiness Report

Tanggal: 7 September 2026

## Target alur

```text
SplashActivity
→ LoginActivity (WAW-owned pre-login UI)
→ LinkedDeviceWebViewActivity (koneksi resmi)
→ tombol KEMBALI
→ MainActivity (WAW-owned shell)
```

## Perubahan yang diselesaikan

| Area | Status | Catatan |
|---|---|---|
| Splash launcher | Lulus kompilasi | Mengarahkan pengguna ke LoginActivity lalu menutup dirinya |
| LoginActivity | Lulus kompilasi | UI WAW sebelum login, tombol koneksi resmi, tanpa meminta password WhatsApp |
| LinkedDeviceWebViewActivity | Lulus kompilasi | Domain dibatasi ke `https://web.whatsapp.com`, session tidak diekspor ke WAW |
| Kembali ke WAW | Ditambahkan | Header linked viewer memiliki tombol `KEMBALI` yang menutup activity dan kembali ke shell sebelumnya |
| MainActivity | Lulus kompilasi | WAW-owned chat/workspace shell |
| Manifest | Diperbaiki | Linking dan Workspace activity didaftarkan; `allowBackup=false` |
| Release signing | Diperketat | Workflow berhenti jika production signing secrets belum tersedia |

## Verifikasi lokal

Perintah:

```text
./gradlew testDebugUnitTest :app:assembleRelease :user-remote:assembleRelease --no-daemon
```

Hasil: `BUILD SUCCESSFUL`.

Artefak lokal yang terbentuk masih **unsigned**, karena signing key production tidak tersedia di sandbox:

| Artefak | SHA-256 |
|---|---|
| `app-release-unsigned.apk` | `8ccff62de4fa92a162a3e2b01f84d1ec4e221df3b6efb7167124afdd15338fd9` |
| `user-remote-release-unsigned.apk` | `87179a372fe913c1922822ddade5a964125ab4b28131367e1ebc9f7da8ccfa49` |

Secret pattern scan dan `git diff --check` lulus.

## Status release yang jujur

Kode dan build pipeline berada pada status **release candidate**, bukan APK Play Store final. APK production belum dapat dinyatakan siap sampai empat secret signing production berikut tersedia pada GitHub Actions environment `production`:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Selain itu, pengujian perangkat nyata masih diperlukan untuk mengonfirmasi izin kamera/mikrofon, login/linking resmi, WebView session, tombol `KEMBALI`, back navigation, dan kembali ke MainActivity. Tidak ada token, cookie, QR/session secret, database, atau protokol privat yang disalin ke WAW.

## Batas fitur

Shell WAW dan koneksi resmi sudah dipisahkan dengan benar. Namun sistem belum menjadi messenger native multi-tenant penuh. Chat, media, push notification, delivery/read receipt, call, status, search, account isolation lintas perangkat, dan Play Store compliance tetap memerlukan pekerjaan lanjutan.
