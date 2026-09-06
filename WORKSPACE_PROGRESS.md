# WAW Workspace — Progress & Implementation Plan

## Prinsip

Setiap fitur mengikuti `DESIGN → IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK → NEXT`.

## Status Saat Ini

- **W0 Foundation:** IMPLEMENTED — CI validation tersedia.
- **W1 File Manager:** IMPLEMENTED — menunggu validasi device.
- **W2 Document + PDF Core:** IMPLEMENTED — menunggu validasi device.
- **W4 Custom Watermark:** IMPLEMENTED — menunggu CI dan validasi device.
- **W10 Remote PC / Android:** IMPLEMENTED — mengikuti authorization boundary.

## W4 — WAW Watermark

Implemented:

- [x] Watermark tersedia langsung dari Workspace WAW.
- [x] Field label dan isi dapat ditambah, dihapus, dan dikustomisasi.
- [x] Timestamp otomatis dari perangkat.
- [x] Lokasi GPS aktual dengan runtime permission.
- [x] Arah kompas dari sensor rotation vector bila tersedia.
- [x] Logo perusahaan atau brand pengguna.
- [x] Branding permanen WAW dan `Made by Frostbyte Tech Ltd`.
- [x] Ekspor PNG melalui Android Storage Access Framework.
- [x] Watermark hanya didistribusikan melalui WAW; tidak ada paket eksternal yang menjadi sumber resmi.
- [ ] Position, size, opacity, rotation, color, dan preset lanjutan.
- [ ] Real-device validation.
- [ ] PASS.
- [ ] LOCK.

## Batas keamanan

Workspace hanya mengakses lokasi yang dipilih dan diizinkan pengguna melalui Android Storage Access Framework. Lokasi GPS hanya dibaca setelah izin runtime diberikan. WAW tidak menyimpan credential WhatsApp, token privat, cookie, private key, atau session rahasia.

## Definition of Done

Stage dapat menjadi `LOCKED` setelah implementasi, build sukses, test otomatis, validasi device, failure-case testing, security review, dokumentasi, dan tidak ada blocker yang diketahui.
