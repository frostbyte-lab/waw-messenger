package com.waw.messenger.linked

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.waw.messenger.security.WawShield
import com.waw.messenger.remote.RemoteHostActivity

/**
 * Official-first WhatsApp linked viewer.
 * The app loads only WhatsApp Web and never exports cookies/session data to WAW.
 */
open class LinkedDeviceWebViewActivity : FragmentActivity() {
    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private lateinit var dashboard: LinearLayout
    private lateinit var headerChrome: LinearLayout
    private lateinit var bottomChrome: LinearLayout
    private lateinit var chatComposer: LinearLayout
    private var pendingFileCallback: ValueCallback<Array<Uri>>? = null
    private var pendingWebPermission: PermissionRequest? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.CAMERA] == true || grants[Manifest.permission.RECORD_AUDIO] == true) {
            loadOfficialWhatsApp()
        } else {
            loadOfficialWhatsApp()
        }
    }

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = pendingFileCallback ?: return@registerForActivityResult
        pendingFileCallback = null
        val uris = if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val clip = data?.clipData
            when {
                clip != null -> Array(clip.itemCount) { clip.getItemAt(it).uri }
                data?.data != null -> arrayOf(data.data!!)
                else -> null
            }
        } else null
        callback.onReceiveValue(uris)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (intent.getBooleanExtra(EXTRA_SKIP_INITIAL_LOAD, false)) {
            setContentView(FrameLayout(this))
            return
        }
        webView = WebView(this)
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(webView, FrameLayout.LayoutParams(-1, -1))
        }
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
        configureWebView()
        addBlueprintChrome()
        addBlueprintDashboard()
        addWawChatComposer()
        setLinkedChromeVisible(false)
        requestRuntimePermissionsIfNeeded()
    }

    private fun addBlueprintChrome() {
        val green = Color.rgb(0, 150, 90)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 14, 12, 8)
            setBackgroundColor(Color.WHITE)
            val brand = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            brand.addView(ImageView(context).apply {
                setImageResource(com.waw.messenger.R.drawable.waw_main_logo)
                contentDescription = "Logo WAW"
            }, LinearLayout.LayoutParams(44, 44))
            brand.addView(TextView(context).apply {
                text = "WAW  BUSINESS"
                textSize = 21f
                setTextColor(Color.rgb(20, 30, 35))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(10, 0, 0, 0)
            }, LinearLayout.LayoutParams(0, 48, 1f))
            brand.addView(TextView(context).apply {
                text = "KEMBALI"
                textSize = 11f
                setTextColor(Color.rgb(0, 125, 75))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(10, 8, 10, 8)
                setOnClickListener { finish() }
            }, LinearLayout.LayoutParams(-2, 48))
            addView(brand, LinearLayout.LayoutParams(-1, 48))
            addView(TextView(context).apply {
                text = "Official Linked Viewer  •  Lokal & Aman"
                textSize = 13f
                setTextColor(Color.rgb(0, 125, 75))
            })
            val tabs = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            listOf("\uf075" to "Chat", "\uf2a0" to "Panggilan", "\uf1ea" to "Status", "\uf1b3" to "Fitur", "\uf07b" to "Workspace").forEach { (icon, label) ->
                val tab = TextView(context).apply {
                    FaText.set(this, context, icon, label)
                    textSize = 12f
                    maxLines = 1
                    setTextColor(if (label.endsWith("Chat")) Color.WHITE else Color.DKGRAY)
                    background = GradientDrawable().apply {
                        setColor(if (label.endsWith("Chat")) Color.rgb(20, 35, 45) else Color.rgb(245, 247, 248))
                        cornerRadius = 10f
                    }
                    gravity = android.view.Gravity.CENTER
                    setPadding(4, 10, 4, 10)
                    setOnClickListener {
                        when {
                            label.endsWith("Workspace") -> startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java))
                            label.endsWith("Fitur") -> startActivity(Intent(this@LinkedDeviceWebViewActivity, RemoteHostActivity::class.java))
                        }
                    }
                }
                tabs.addView(tab, LinearLayout.LayoutParams(0, 44, 1f).apply { setMargins(4, 10, 4, 0) })
            }
            addView(tabs, LinearLayout.LayoutParams(-1, 54))
            addView(TextView(context).apply {
                text = "● TERHUBUNG  •  WhatsApp Web resmi"
                textSize = 10f
                setTextColor(Color.rgb(0, 145, 85))
                setPadding(2, 3, 0, 0)
            })
        }
        headerChrome = header
        root.addView(header, FrameLayout.LayoutParams(-1, 154, Gravity.TOP))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            listOf("\uf075" to "Chat", "\uf2a0" to "Panggilan", "\uf1ea" to "Status", "\uf1b3" to "Fitur", "\uf07b" to "Workspace").forEach { (icon, label) ->
                addView(TextView(context).apply {
                    FaText.set(this, context, icon, label)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setTextColor(if (label.endsWith("Workspace")) green else Color.DKGRAY)
                    setPadding(4, 12, 4, 12)
                    setOnClickListener {
                        when {
                            label.endsWith("Workspace") -> startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java))
                            label.endsWith("Fitur") -> startActivity(Intent(this@LinkedDeviceWebViewActivity, RemoteHostActivity::class.java))
                        }
                    }
                }, LinearLayout.LayoutParams(0, 60, 1f))
            }
        }
        bottomChrome = bottom
        root.addView(bottom, FrameLayout.LayoutParams(-1, 68, Gravity.BOTTOM))
    }

    private fun addBlueprintDashboard() {
        dashboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 12)
            setBackgroundColor(Color.WHITE)
            addView(TextView(context).apply {
                text = "Workspace Quick Access"
                textSize = 17f
                setTextColor(Color.rgb(25, 35, 40))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "Kelola dokumen, tugas, kalender, dan file lokal WAW"
                textSize = 13f
                setTextColor(Color.DKGRAY)
                setPadding(0, 4, 0, 12)
            })
            val grid = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            listOf(
                listOf("▤  Dokumen", "Editor teks & PDF", false),
                listOf("✓  Tugas", "Catatan absensi & pekerjaan", false),
                listOf("▦  Kalender", "Agenda lokal", false),
                listOf("▱  File", "File Manager Workspace", true)
            ).chunked(2).forEach { rowItems ->
                val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                rowItems.forEach { item ->
                    val card = TextView(context).apply {
                        text = "${item[0]}\n${item[1]}"
                        textSize = 14f
                        setTextColor(Color.rgb(30, 35, 40))
                        setBackgroundColor(Color.rgb(247, 249, 250))
                        setPadding(18, 18, 12, 18)
                        setOnClickListener {
                            if (item[2] as Boolean) startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java))
                        }
                    }
                    row.addView(card, LinearLayout.LayoutParams(0, 100, 1f).apply { setMargins(0, 0, 8, 10) })
                }
                grid.addView(row)
            }
            addView(grid, LinearLayout.LayoutParams(-1, -2))
            addView(TextView(context).apply {
                text = "Buka Chat WhatsApp Web"
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(0, 150, 90))
                setPadding(12, 16, 12, 16)
                setOnClickListener { dashboard.visibility = android.view.View.GONE }
            }, LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 12, 0, 0) })
        }
        val params = FrameLayout.LayoutParams(-1, -1).apply {
            topMargin = 154
            bottomMargin = 68
        }
        dashboard.visibility = android.view.View.GONE
        root.addView(dashboard, params)
    }

    private fun setLinkedChromeVisible(visible: Boolean) {
        val state = if (visible) android.view.View.VISIBLE else android.view.View.GONE
        if (::headerChrome.isInitialized) headerChrome.visibility = state
        if (::bottomChrome.isInitialized) bottomChrome.visibility = state
        if (::dashboard.isInitialized) dashboard.visibility = android.view.View.GONE
        if (::chatComposer.isInitialized) chatComposer.visibility = state
        if (visible && ::headerChrome.isInitialized) {
            AlphaAnimation(0f, 1f).apply { duration = 260; fillAfter = true }.also { headerChrome.startAnimation(it) }
            if (::bottomChrome.isInitialized) AlphaAnimation(0f, 1f).apply { duration = 320; fillAfter = true }.also { bottomChrome.startAnimation(it) }
        }
    }

    private fun addWawChatComposer() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 7, 12, 7)
            setBackgroundColor(Color.WHITE)
            elevation = 18f
        }
        val status = TextView(this).apply {
            text = "● TERHUBUNG  •  siap mengirim"
            textSize = 10f
            setTextColor(Color.rgb(0, 145, 85))
            setPadding(4, 0, 0, 3)
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val input = EditText(this).apply {
            hint = "Tulis pesan dengan WAW..."
            textSize = 14f
            singleLine = true
            setPadding(15, 0, 12, 0)
            background = GradientDrawable().apply {
                setColor(Color.rgb(245, 248, 247))
                cornerRadius = 42f
                setStroke(1, Color.rgb(210, 231, 224))
            }
            setOnFocusChangeListener { _, hasFocus ->
                status.text = if (hasFocus) "● SEDANG MENGETIK  •  WAW composer" else "● TERHUBUNG  •  siap mengirim"
            }
        }
        fun action(icon: String, label: String, onClick: () -> Unit) = TextView(this).apply {
            FaText.set(this, context, icon, label)
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(0, 125, 95))
            setPadding(8, 0, 8, 0)
            setOnClickListener { onClick() }
        }
        row.addView(action("\uf03d", "Panggilan video") { clickWhatsAppAction("video") }, LinearLayout.LayoutParams(42, 48))
        row.addView(action("\uf095", "Panggilan suara") { clickWhatsAppAction("voice") }, LinearLayout.LayoutParams(42, 48))
        row.addView(input, LinearLayout.LayoutParams(0, 48, 1f))
        row.addView(action("\uf118", "Emoji") { input.append("🙂") }, LinearLayout.LayoutParams(42, 48))
        row.addView(action("\uf1d8", "Kirim") {
            val message = input.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToWhatsApp(message)
                input.text.clear()
                status.text = "● TERKIRIM  •  melalui koneksi resmi WhatsApp"
            }
        }, LinearLayout.LayoutParams(46, 48))
        panel.addView(status, LinearLayout.LayoutParams(-1, 22))
        panel.addView(row, LinearLayout.LayoutParams(-1, 50))
        chatComposer = panel
        root.addView(panel, FrameLayout.LayoutParams(-1, 78, Gravity.BOTTOM).apply { bottomMargin = 68 })
    }

    private fun sendMessageToWhatsApp(message: String) {
        val escaped = org.json.JSONObject.quote(message)
        webView.evaluateJavascript("""
            (() => {
              const text = $escaped;
              const box = document.querySelector('[contenteditable="true"]');
              if (!box) return false;
              box.focus();
              document.execCommand('insertText', false, text);
              box.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: text }));
              box.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', bubbles: true }));
              return true;
            })();
        """, null)
    }

    private fun clickWhatsAppAction(type: String) {
        val labels = if (type == "video") "video|video call|panggilan video" else "voice|phone|panggilan suara"
        webView.evaluateJavascript("""
            (() => {
              const pattern = /$labels/i;
              const button = [...document.querySelectorAll('button,[role="button"]')]
                .find(node => pattern.test(node.getAttribute('aria-label') || node.textContent || ''));
              if (button) { button.click(); return true; }
              return false;
            })();
        """, null)
    }

    private fun requestRuntimePermissionsIfNeeded() {
        val missing = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
        if (missing.isEmpty()) loadOfficialWhatsApp() else permissionLauncher.launch(missing)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = false
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = DESKTOP_USER_AGENT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("document.body && document.body.innerText", { raw ->
                    val loginScreen = raw?.contains("Pindai untuk login") == true || raw?.contains("Scan to log in") == true
                    setLinkedChromeVisible(!loginScreen)
                })
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return true
                return WawShield.isBlocked(uri) || !isAllowedWhatsAppNavigation(uri)
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                pendingFileCallback?.onReceiveValue(null)
                pendingFileCallback = filePathCallback
                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                return runCatching { filePicker.launch(intent); true }.getOrElse {
                    pendingFileCallback = null
                    false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null || request.origin.host != OFFICIAL_HOST) {
                    request?.deny()
                    return
                }
                val allowed = request.resources.filter {
                    (it == PermissionRequest.RESOURCE_AUDIO_CAPTURE && ContextCompat.checkSelfPermission(this@LinkedDeviceWebViewActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) ||
                        (it == PermissionRequest.RESOURCE_VIDEO_CAPTURE && ContextCompat.checkSelfPermission(this@LinkedDeviceWebViewActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                }.toTypedArray()
                if (allowed.isEmpty()) request.deny() else runOnUiThread {
                    pendingWebPermission = request
                    request.grant(allowed)
                    pendingWebPermission = null
                }
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                if (pendingWebPermission == request) pendingWebPermission = null
                super.onPermissionRequestCanceled(request)
            }
        }
    }

    private fun loadOfficialWhatsApp() {
        if (!::webView.isInitialized) return
        webView.loadUrl(OFFICIAL_URL)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    companion object {
        const val EXTRA_SKIP_INITIAL_LOAD = "com.waw.messenger.extra.SKIP_INITIAL_LOAD"
        private const val OFFICIAL_URL = "https://web.whatsapp.com"
        private const val OFFICIAL_HOST = "web.whatsapp.com"
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        private fun isAllowedWhatsAppNavigation(uri: Uri): Boolean =
            uri.scheme == "https" && uri.host == OFFICIAL_HOST
    }
}
