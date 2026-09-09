# HYB-040 — UI Shell dan Design System Parity

## Perubahan tahap ini

Android WAW mempertahankan satu shell produk dengan header WAW, status resmi WhatsApp, dan lima navigasi utama: Chat, Panggilan, Status, Fitur, dan Workspace. Tab yang belum memiliki backend production menampilkan state kosong yang jujur dan tidak menggunakan data demo atau tombol login palsu.

Chat dan linking tetap diarahkan ke WhatsApp Web resmi. Workspace, Remote, dokumen, watermark, dan alat perangkat tetap diberi label sebagai fitur WAW, bukan kemampuan internal WhatsApp.

## Visual contract

Web dan APK harus mempertahankan branding WAW, warna green/teal, surface putih, typography compact, rounded cards, status indicator, button treatment, dan responsive behavior. Perbedaan native yang diizinkan hanya dialog permission, MediaProjection, Accessibility settings, file picker, safe area, dan back behavior.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Navigation | Lima tujuan utama tersedia dan memiliki label terlihat |
| Official boundary | Chat/Calls/Status linking tidak membuat backend WhatsApp alternatif |
| Empty state | Fitur yang belum tersedia diberi status jelas, tanpa data palsu |
| Mobile | Layout menggunakan scroll/padding native dan tidak mengandalkan lebar desktop |
| Accessibility | Ikon memiliki content description dan tindakan utama memiliki teks |
| Security | UI tidak meminta password, cookie, QR token, atau session secret WhatsApp |

## Catatan pengujian

Build Android dan instrumentation test tetap menjadi required check. Review perangkat nyata pada lebar 320–360 dp direkomendasikan untuk memastikan lima label NavigationBar tidak terlalu padat. Jika padat, implementasi berikutnya harus memakai tab horizontal yang dapat digeser tanpa mengubah channel boundary.
