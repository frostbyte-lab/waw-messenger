# WAW Messenger — Consolidation Index

Dokumen ini menetapkan `waw-messenger` sebagai **repository utama dan sumber kebenaran tunggal** untuk aplikasi WAW Messenger, backend, workflow release, dokumentasi produk, arsitektur, dan keamanan.

## Struktur resmi

| Lokasi | Isi | Status |
|---|---|---|
| `app/` | Aplikasi Android utama | Aktif |
| `backend/` | API, autentikasi, database, chat, dan WebSocket | Aktif |
| `docs/product/` | PRD, roadmap, workspace specification, dan TODO | Aktif |
| `docs/architecture/` | Arsitektur dan modul teknis | Aktif |
| `docs/ui/` | Panduan tampilan dan design reference | Aktif |
| `docs/integration/` | Batas integrasi resmi dan kebijakan pihak ketiga | Aktif |
| `.github/workflows/` | CI, Android artifact, dan deployment Worker | Aktif |

## Dokumen yang dikonsolidasikan

| Sumber | Lokasi baru | Keterangan |
|---|---|---|
| `waw-android/docs/product/PRD.md` | `docs/product/WAW_ANDROID_PRD.md` | PRD workspace Android |
| `waw-android/docs/architecture/ARCHITECTURE.md` | `docs/architecture/WAW_ANDROID_ARCHITECTURE.md` | Arsitektur awal |
| `waw-android/docs/modules/README.md` | `docs/architecture/WAW_ANDROID_MODULES.md` | Daftar modul |
| `waw-android/docs/ui/UI_GUIDE.md` | `docs/ui/WAW_ANDROID_UI_GUIDE.md` | Panduan UI |
| `wa-workspace-v4-2-saas-lengkap/CATATAN.md` | `docs/product/WA_WORKSPACE_SPEC.md` | Spesifikasi workspace dan batas integrasi |
| `wa-workspace-v4-2-saas-lengkap/todo.md` | `docs/product/WA_WORKSPACE_TODO.md` | TODO workspace |

## Aturan repository

Pengembangan kode baru, issue, pull request, workflow, secret reference, dan dokumentasi wajib ditujukan ke `waw-messenger`. Repository pendukung tidak digunakan untuk commit fitur baru setelah konsolidasi ini.

Repository sumber **belum dihapus** dan belum diubah menjadi arsip pada Tahap 1. Penghapusan atau pengarsipan hanya dilakukan setelah isi konsolidasi diverifikasi dan pengguna memberikan persetujuan untuk langkah tersebut.

## Aturan konflik spesifikasi

Jika dokumen lama bertentangan dengan implementasi aktif, keputusan berikut berlaku:

1. Kontrak API dan kode aktif di `waw-messenger/backend/` menjadi sumber teknis utama.
2. `README.md`, `ROADMAP.md`, dan dokumen status di repository utama menjadi sumber status implementasi.
3. Dokumen yang dipindahkan dari repository lama menjadi referensi produk dan harus diberi status `planned`, `in progress`, atau `implemented` sebelum dipakai sebagai klaim fitur.
4. Tidak ada fitur yang dianggap selesai hanya karena tercantum di PRD atau TODO.
5. Integrasi WhatsApp hanya boleh melalui jalur resmi dan tidak boleh menyalin protokol privat, token, cookie, atau session pihak lain.

## Target berikutnya

Tahap berikutnya adalah menyelaraskan roadmap dan TODO menjadi backlog tunggal, lalu memetakan setiap item ke issue atau pull request di `waw-messenger`. Setelah itu, repository lama dapat diarsipkan dengan tautan yang menunjuk ke dokumen konsolidasi.
