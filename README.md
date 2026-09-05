WAW Messenger - Opsi A (Official WhatsApp Linked Path)
Jalur 100% resmi. WAW tidak membuat protokol WhatsApp sendiri. Semua fitur WhatsApp berasal dari web.whatsapp.com resmi. Workspace adalah fitur tambahan milik Frostbyte Lab.
1. Prinsip
Official-First: WAW hanya memuat https://web.whatsapp.com di WebView. Tidak ada Baileys, whatsapp-web.js, atau reverse-engineer.
No Credential Harvesting: Tidak meminta password WA, tidak mengambil token, cookie, private key.
Workspace = WAW-Owned: File Manager, PDF Tools, Scanner, Watermark, Vault, Shield, Notes, dll adalah milik WAW dan tidak menggantikan backend WhatsApp.
Blocked Means Blocked: Jika WhatsApp Web belum support fitur, tampilkan NOT_SUPPORTED bukan bypass.
2. Arsitektur
HP Utama (WhatsApp Resmi)
    |
    | Link a Device (QR / 8-digit code)
    v
WAW App [ LinkedDeviceWebViewActivity ]
    |
    |---> web.whatsapp.com (official)
    |       |-- Chats, Groups, Media, Voice Note, Poll, Status, Calls (1-1)
    |
    '---> WAW Workspace (Frostbyte)
            |-- File Manager, PDF, Scanner, Watermark
            |-- Secure Vault
            |-- Fingerprint Attendance
            |-- WAW Shield (Anti-Judol/Phishing)
            |-- Notes, Tasks, Backup, Remote, Universal Search
3. Setup Android agar Fitur Asli WA Jalan 100%
3.1 AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.READ_CONTACTS" /> <!-- opsional, untuk Contact Picker WAW -->

<application
    android:usesCleartextTraffic="false"
    ...>
    <activity
        android:name=".linked.LinkedDeviceWebViewActivity"
        android:hardwareAccelerated="true" />
</application>
3.2 LinkedDeviceWebViewActivity.kt - Core
package com.waw.messenger.linked

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LinkedDeviceWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        permissionLauncher.launch(arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ))

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            // User-Agent Desktop WAJIB agar fitur Call & Status muncul
        }

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Jangan biarkan link keluar WebView tanpa filter Shield
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                // WAJIB untuk Call & VC
                runOnUiThread {
                    request.grant(request.resources)
                }
            }
            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                super.onPermissionRequestCanceled(request)
            }
        }

        // Muat WhatsApp Web Resmi
        webView.loadUrl("https://web.whatsapp.com")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
3.3 Kenapa User-Agent Desktop WAJIB
WhatsApp Web menyembunyikan tombol Call, VC, dan Status Composer jika mendeteksi Mobile UA. Pakai Desktop Chrome UA agar semua fitur asli muncul.
4. Mapping Fitur Asli WA ke Opsi A
Fitur Asli WA
Status di Opsi A
Cara Verifikasi
Chat 1-1, Group, Broadcast
✅ 100%
Langsung dari web.whatsapp.com
Media: Foto, Video, Doc, Audio, PTT
✅ 100%
Upload via WebView file chooser
Voice Note / Voice Message
✅ 100%
Butuh RECORD_AUDIO permission
Poll, Sticker, Reaction, Reply, Edit, Delete
✅ 100%
Native Web
Status / Updates: Lihat, Balas, Posting
✅ 100%
Tab Status di Web. Dulu hanya lihat, per Q4 2024 sudah bisa posting
Calls: Voice Call & Video Call 1-1
✅ BISA (Beta Rollout 2025)
Butuh grant onPermissionRequest + user kebagian flag beta Web. Jika belum kebagian: tampilkan banner "Calling belum tersedia di akun ini - gunakan WhatsApp resmi untuk call"
Group Call, Screen Share, Calls History
🟡 Bertahap
Muncul jika akun sudah dapat flag Calls Tab di Web
Live Location Share
❌ NOT_SUPPORTED
Memang tidak didukung di Web resmi. Tampilkan BLOCKED
Companion Linked Device Management
✅ Bisa
via Menu Web > Linked Devices
5. Flow Linking Resmi (User-facing)
User install WAW.
WAW tampilkan LinkedDeviceWebViewActivity -> loading web.whatsapp.com -> muncul QR.
Instruksi di atas WebView:
Buka WhatsApp di HP utama > Titik 3 > Perangkat Tertaut > Tautkan Perangkat > Scan QR ini Alternatif: Tautkan dengan nomor telepon > Masukkan kode 8 digit
Setelah linked, session disimpan oleh WebView via CookieManager (bukan oleh WAW backend).
Jangan upload cookie/session ke backend worker.js. Session tetap lokal.
6. Batasan Keamanan & Compliance
backend/worker.js & migrations/*.sql yang ada sekarang (legacy auth/chat) HARUS DIARSIPKAN / DINONAKTIFKAN untuk Track A. Jangan pakai untuk simpan chat WA.
Workspace data (file, PDF, vault, attendance records) simpan di storage terenkripsi WAW sendiri, pisah total dari data WhatsApp. Fingerprint hanya dipakai untuk verifikasi absensi, bukan untuk mengunci Workspace atau Vault.
Implement WAW Shield: sebelum WebView load URL eksternal dari chat, cek domain ke blocklist judol/phishing milik WAW.
IP Info hanya untuk diagnostics, jangan klaim sebagai lokasi GPS orang lain.
7. Definition of Done Opsi A
WebView bisa login via QR & via 8-digit code
Chat sync, media upload/download jalan di device real
Voice Note rekam & play jalan
Status lihat & posting jalan
Permission Call di-grant, test call 1-1 jika flag beta ada, jika tidak ada tampilkan NOT_SUPPORTED yang jujur
Cookie tidak dikirim ke backend legacy
Workspace toolbox bisa dipanggil overlay tanpa ganggu WebView
Build release sukses, no secret di logcat
8. Catatan Produk
WAW Messenger bukan pengganti WhatsApp. WAW adalah: WhatsApp Linked Viewer (resmi) + WAW Workspace (milik Frostbyte).
Jika Meta membuka Companion SDK resmi di masa depan, Track A bisa di-upgrade dari WebView menjadi Native Companion tanpa mengubah Workspace.
Frostbyte Lab - Build carefully. Official-first.
