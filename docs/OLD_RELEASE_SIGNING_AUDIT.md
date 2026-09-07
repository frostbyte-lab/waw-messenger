# Audit Signing Release Lama

Tanggal audit: 7 September 2026

## Hasil

Release GitHub terbaru yang diperiksa adalah `user-remote-0.1.1`, dipublikasikan 7 September 2026. Release tersebut menyediakan:

```text
waw-release.apk
user-remote-release.apk
SHA256SUMS.txt
```

Kedua APK lama berhasil diverifikasi oleh `apksigner` menggunakan APK Signature Scheme v2 dan v3.

## Sertifikat yang ditemukan

| Field | Nilai |
|---|---|
| Certificate DN | `CN=WAW Messenger, OU=Android, O=frostbyte-lab, L=Unknown, ST=Unknown, C=ID` |
| SHA-256 certificate digest | `2501bdbc369c8ff06462e60408419f3509fa93d9e9370c79e10f0d571c59b097` |
| SHA-1 certificate digest | `f9e2784e1c4199f674008b15b554dd6a95e7f7f5` |
| APK signature | Valid, v2/v3 |

SHA-256 file digest:

```text
waw-release.apk          f6568cd2556e3cb360810538898a34b4c7d3f51ba229ff9afeccc49905ca6c8f
user-remote-release.apk  e9da4e0fb09ceff2cb4178966eec30ba0921d70761c3d305aa4367c9e3f6544a
```

## Keputusan

**Jangan membuat signing key baru** untuk update aplikasi ini sebelum dipastikan key lama hilang. APK hanya mengandung sertifikat publik; private key, alias, dan password tidak dapat dipulihkan dari APK.

Release baru harus ditandatangani dengan keystore yang menghasilkan certificate digest yang sama:

```text
2501bdbc369c8ff06462e60408419f3509fa93d9e9370c79e10f0d571c59b097
```

Secret GitHub tidak dapat dibaca oleh sesi audit ini karena API mengembalikan HTTP 403 untuk daftar secrets. Itu tidak membuktikan secret hilang. Nama secret yang diperlukan workflow tetap:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Pemilik repository perlu memastikan keempat secret tersebut menunjuk ke keystore lama yang menghasilkan fingerprint di atas. Jika tidak ada backup keystore lama, update dengan application ID yang sama berisiko ditolak Android/Play Store karena certificate berbeda.
