# WAW Product Requirements Document

## Ringkasan

WAW adalah workspace produktivitas Android yang menempatkan alat kerja berdampingan dengan akses berbagi ke WhatsApp resmi. Nilai utama produk adalah mengurangi perpindahan aplikasi ketika pengguna perlu membuat, mengolah, lalu mengirim dokumen kerja.

## Pengguna Sasaran

Pengguna sasaran adalah pekerja lapangan, pemilik usaha kecil, pelajar, dan tim operasional yang sering menerima dokumen melalui WhatsApp, memindai dokumen fisik, mengubah format file, atau mengakses komputer dari perangkat mobile.

## MVP

| Area | Kebutuhan MVP | Kriteria penerimaan |
|---|---|---|
| App shell | Header profil, menu, dan lima tab | Pengguna dapat berpindah tab tanpa kehilangan state dasar. |
| Work Space | Grid delapan kartu fitur | Setiap kartu membuka halaman detail atau status modul yang jelas. |
| Scan dokumen | Pemilihan gambar/kamera, preview, ekspor PDF | Pengguna dapat menghasilkan file PDF lokal dan melihat nama file. |
| Berbagi | Android Share Sheet ke WhatsApp resmi | File dapat dibagikan tanpa WAW mengakses isi chat WhatsApp. |
| Convert gambar | Pilih gambar dan simpan hasil konversi yang didukung | Hasil tersimpan di penyimpanan aplikasi atau lokasi pilihan pengguna. |
| Print | Mengirim dokumen ke print service Android | Sistem menampilkan dialog print resmi bila tersedia. |
| Biometrik | Kunci/buka workspace dengan biometrik perangkat | Fitur hanya aktif setelah persetujuan dan perangkat mendukungnya. |

## Fitur Fase Berikutnya

Remote access, berbagi layar, pelacakan lokasi, dan sinkronisasi lintas perangkat memerlukan desain protokol, otorisasi, audit keamanan, dan pengujian lebih lanjut. Fitur tersebut tidak boleh dianggap selesai hanya karena layar prototipe sudah tersedia.

## Prinsip UX

Antarmuka mengikuti mockup yang diberikan: latar terang hangat, kartu krem pucat, aksen biru tua, judul tebal, grid dua kolom, dan bottom navigation lima item. Setiap aksi penting memberikan feedback visual; tindakan yang memerlukan izin menjelaskan alasan izin sebelum dialog sistem ditampilkan.

## Non-Goals

WAW tidak akan menjadi klien WhatsApp alternatif, tidak akan menyediakan inbox atau database chat tiruan, tidak akan mengirim pesan otomatis, dan tidak akan mengelola akun WhatsApp pengguna di luar mekanisme resmi yang disediakan Android atau WhatsApp.

## Roadmap

| Fase | Fokus |
|---|---|
| 0 | Struktur repository, dokumentasi, dan design tokens. |
| 1 | UI shell, navigasi, halaman Work Space, dan detail modul. |
| 2 | Scan dokumen lokal, PDF, convert gambar, dan share ke WhatsApp. |
| 3 | Print, biometrik, pemutar MP3, dan penyempurnaan aksesibilitas. |
| 4 | Riset remote access dan screen sharing dengan threat model yang terdokumentasi. |
