WAW WORKSPACE - CATATAN LENGKAP FINAL (VERSI WEB UJI COBA)
System UI Tetap, Jalan Kayak WA Pada Umumnya
URL Live: https://waw-workspace-review.pages.dev/
Halaman Aktif: /data-deletion/ (bukti fitur koneksi, kontak, percakapan, pesan, webhook)
Prinsip: UI ASLI WAW DIPERTAHANKAN 100%, LOGIN CUMA OTP, FITUR JALAN KAYAK WA BIASA

⚠️ PENTING: VERSI WEB INI HANYA SYSTEM UJI COBA
Versi web yang ada di https://waw-workspace-review.pages.dev/ ini HANYA UNTUK UJI COBA, CHECK, DAN PREVIEW DETAIL SAJA.
Jika semua berjalan lancar di versi web, maka VERSI UTAMANYA ADA PADA VERSI APK, sesuai dengan tampilan / fitur / system yang ada di web ini.
Web = Preview & Testing | APK = Produk Utama

1. TUJUAN VERSI WEB UJI COBA
Fungsi Versi Web:

Untuk check apakah login OTP berjalan
Untuk preview detail UI WAW (apakah layout, warna, bubble chat sudah sesuai)
Untuk test fitur: koneksi, kontak, percakapan, pesan, webhook, call, VC
Untuk demo ke tim / calon user sebelum install APK
Untuk lolos verifikasi domain di Meta (karena butuh URL live)
Bukan Untuk:

Bukan produk utama
Bukan untuk pemakaian harian skala besar
Data di web hanya untuk testing, data asli ada di APK
Flow Pengembangan:

Versi WEB Uji Coba (sekarang) -> Check & Preview -> OK -> Build VERSI APK Utama
Semua tampilan, fitur, system yang ada di web ini akan 100% sama di versi APK nanti. Jadi apa yang dilihat di web = apa yang didapat di APK.

2. LOGIN - SIMPLE OTP SAJA (Tanpa PT/NIB/Domain)
Flow Kayak WA Asli:

Buka WAW Web Uji Coba -> Input No HP 0823... -> Kirim OTP -> Input 6 digit OTP -> Langsung Masuk Preview Workspace
Tidak Ada:

Password, Email verifikasi, Upload NIB/NPWP, Verifikasi domain, Nunggu 3 hari approval
Teknologi: Firebase Phone Auth (gratis)

js
signInWithPhoneNumber(auth, phoneNumber) // Kirim OTP
confirmationResult.confirm(otp) // Masuk preview workspace
3. SYSTEM UI WAW YANG DIPERTAHANKAN (JANGAN DIHAPUS) - WEB & APK SAMA
A. Layout Utama (Tetap Kayak WA Web & Nanti di APK Sama)
[HEADER] WAW Workspace | Search | Call | VC | Titik Tiga
[SIDEBAR KIRI 30%]          [CHAT KANAN 70%]
- Search Chat               - Nama Kontak + Status Online
- Filter: Semua, Belum      - Bubble Chat (kayak WA)
  Dibaca, Grup              - Input: emoji, file, voice note
- List Chat                 - Tombol: Call & Video Call
Catatan: Layout ini di versi web uji coba, dan akan sama persis di versi APK utama. Jadi user yang sudah nyaman di web tidak perlu adaptasi lagi di APK.

B. Fitur Wajib Kayak WA Pada Umumnya (Harus Ada di Web Uji Coba & APK Utama)
1. Koneksi

Status: Terhubung, Menghubungkan, Offline
Data: koneksi
2. Kontak

List kontak dengan foto profil, nama, no HP
Data: kontak
3. Percakapan / Chat (Inti WA)

Bubble chat kiri/kanan, centang 1/2/biru
Kirim: teks, gambar, video, file, voice note
Data: percakapan, pesan
4. Panggilan & Video Call (VC)

Voice Call & Video Call di header chat
Teknologi: WebRTC / Agora
5. Grup, Status, Template, Webhook, Pengaturan

Semua fitur workspace asli tetap ada
Semua fitur di atas: Ada di versi web uji coba untuk preview, dan wajib ada juga di versi APK utama dengan tampilan/system yang sama.

4. PERBEDAAN WEB UJI COBA vs APK UTAMA
Aspek	Versi WEB Uji Coba	Versi APK Utama
Tujuan	Check, Preview, Testing	Produk utama, pemakaian harian
URL / Lokasi	https://waw-workspace-review.pages.dev/	File .apk di Play Store / direct install
Login	OTP Firebase (sama)	OTP Firebase (sama)
Tampilan UI	UI WAW asli	UI WAW asli - SAMA PERSIS
Fitur	Semua fitur untuk di-check	Semua fitur untuk dipakai - SAMA PERSIS
Data	Data testing / dummy	Data asli user
Koneksi	Butuh browser & internet	Native, bisa background service, notifikasi push
Call/VC	WebRTC via browser	Native WebRTC, lebih stabil
Status	Sementara, untuk preview	Permanen, versi utama
Prinsip: Apa yang jalan di web uji coba, harus jalan juga di APK utama dengan tampilan/fitur/system yang identik.

5. CARA KERJA LENGKAP
Tahap 1: Web Uji Coba (Sekarang)

User buka https://waw-workspace-review.pages.dev/
Input HP -> OTP -> Masuk preview workspace
Check: Apakah UI sudah oke? Chat jalan? Call/VC jalan?
Feedback & perbaikan di web
Tahap 2: APK Utama (Jika Web OK)

Build APK dengan tampilan/fitur/system yang SAMA PERSIS dengan web uji coba
User install APK
Login OTP sama, langsung masuk workspace
Semua chat, kontak, riwayat dari web (jika di-sync) bisa ada di APK
APK jadi versi utama untuk pemakaian sehari-hari
6. FILE YANG HARUS ADA
Di Web Uji Coba (pages.dev):

index.html (login OTP) - JANGAN 404
/workspace/ (UI asli WAW)
/data-deletion/ (SUDAH ADA - pertahankan)
/privacy/, /terms/
Di APK Utama Nanti:

Semua screen yang ada di web: Login OTP, Workspace, Kontak, Chat, Calls, Settings
Tampilan, fitur, system identik dengan web uji coba
7. KESIMPULAN AKHIR
Versi web ini hanya system uji coba saja, untuk check/preview detail
System UI WAW dipertahankan 100% - tidak ada fitur yang dihapus
Jalan kayak WA pada umumnya - chat, call, VC, grup, centang biru
Jika semua berjalan di web, maka versi utamanya ada pada versi APK sesuai tampilan/fitur/system web nya
Login tetap simple: No HP + OTP saja, tanpa PT/NIB
Web = Untuk Preview | APK = Untuk Pakai

Semua yang diuji di web, akan ada di APK utama dengan tampilan yang sama persis.

Link:

Web Uji Coba: https://waw-workspace-review.pages.dev/
Data Deletion (sudah live): https://waw-workspace-review.pages.dev/data-deletion/
Email Penghapusan: projekmii23@gmail.com
