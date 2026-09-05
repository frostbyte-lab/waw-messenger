# Panduan UI/UX WAW

## Arah Visual

WAW menggunakan tampilan produktivitas yang bersih dan tenang. Gunakan latar putih atau ivory, kartu dengan warna krem sangat pucat, border abu-abu tipis, teks utama hampir hitam, teks sekunder abu-abu, dan aksen biru tua untuk tab aktif serta tindakan utama.

| Token | Nilai awal | Penggunaan |
|---|---|---|
| Background | `#F7F7F1` | Latar aplikasi. |
| Surface | `#FFFFFF` | Kontainer utama dan header. |
| Card | `#F3F2EB` | Kartu fitur Workspace. |
| Foreground | `#202124` | Judul dan label utama. |
| Muted | `#6B6B6B` | Deskripsi dan label sekunder. |
| Primary | `#164C78` | Tab aktif, tombol utama, dan indikator fokus. |
| Border | `#D9D9D1` | Garis pembatas dan kontur. |

## Halaman Work Space

Header berisi ikon menu di kiri dan avatar profil berbentuk lingkaran di kanan. Judul halaman adalah “Work space” dengan deskripsi singkat “Fitur baru yang belum ada di WhatsApp.” Di bawahnya terdapat grid dua kolom dengan delapan kartu: Remote, Scan dokumen, Fingerprint, Lacak lokasi, Convert gambar, Print document, Berbagi layar, dan Pemutar mp3.

Bottom navigation berisi lima tab: Chat, Panggilan, Status, Work space, dan Fitur. Work space menjadi tab aktif dengan teks dan outline berwarna biru tua. Ikon dapat menggunakan simbol sederhana dan konsisten; jangan meniru ikon proprietary WhatsApp secara berlebihan.

## States dan Aksesibilitas

Setiap kartu harus memiliki state normal, pressed, disabled, loading, dan unavailable. Teks tidak boleh hanya dibedakan dengan warna. Ukuran target sentuh minimum harus nyaman untuk perangkat Android, dan label harus tetap terbaca ketika ukuran font sistem diperbesar.

## Layar Detail Fitur

Layar detail menampilkan judul, penjelasan satu paragraf, status izin atau kemampuan perangkat, tombol tindakan utama, serta tombol bantuan atau pembatalan. Fitur yang belum siap harus diberi label “Segera hadir” dan tidak menampilkan aksi palsu yang tidak berfungsi.
