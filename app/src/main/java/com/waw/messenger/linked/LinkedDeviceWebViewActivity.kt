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
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

/**
 * Official-first WhatsApp linked viewer.
 * The app loads only WhatsApp Web and never exports cookies/session data to WAW.
 */
open class LinkedDeviceWebViewActivity : FragmentActivity() {
    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
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
        configureWebView()
        addBlueprintChrome()
        requestRuntimePermissionsIfNeeded()
    }

    private fun addBlueprintChrome() {
        val green = Color.rgb(0, 150, 90)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 14, 12, 8)
            setBackgroundColor(Color.WHITE)
            addView(TextView(context).apply {
                text = "◉  WAW  BUSINESS"
                textSize = 21f
                setTextColor(Color.rgb(20, 30, 35))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "WhatsApp Workspace"
                textSize = 13f
                setTextColor(Color.DKGRAY)
            })
            val tabs = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            listOf("Chat", "Panggilan", "Status", "Fitur", "Workspace").forEach { label ->
                val tab = TextView(context).apply {
                    text = label
                    textSize = 12f
                    setTextColor(if (label == "Chat") Color.WHITE else Color.DKGRAY)
                    setBackgroundColor(if (label == "Chat") Color.rgb(20, 35, 45) else Color.rgb(245, 247, 248))
                    gravity = android.view.Gravity.CENTER
                    setPadding(18, 10, 18, 10)
                    setOnClickListener {
                        if (label == "Workspace") startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java))
                    }
                }
                tabs.addView(tab, LinearLayout.LayoutParams(0, 44, 1f).apply { setMargins(4, 10, 4, 0) })
            }
            addView(tabs, LinearLayout.LayoutParams(-1, 54))
        }
        root.addView(header, FrameLayout.LayoutParams(-1, 154, Gravity.TOP))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            listOf("Chat", "Panggilan", "Status", "Fitur", "Workspace").forEach { label ->
                addView(TextView(context).apply {
                    text = label
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setTextColor(if (label == "Workspace") green else Color.DKGRAY)
                    setPadding(4, 12, 4, 12)
                    setOnClickListener {
                        if (label == "Workspace") startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java))
                    }
                }, LinearLayout.LayoutParams(0, 60, 1f))
            }
        }
        root.addView(bottom, FrameLayout.LayoutParams(-1, 68, Gravity.BOTTOM))
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
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = DESKTOP_USER_AGENT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return true
                return !isAllowedWhatsAppNavigation(uri)
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
                    it == PermissionRequest.RESOURCE_AUDIO_CAPTURE || it == PermissionRequest.RESOURCE_VIDEO_CAPTURE
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
