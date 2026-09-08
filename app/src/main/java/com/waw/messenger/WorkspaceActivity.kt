package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

/** Local WAW HTML dashboard; it never loads WhatsApp Web or an external chat UI. */
class WorkspaceActivity : FragmentActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript("""
                        (() => {
                          document.querySelectorAll('button,[role="button"]').forEach((el) => {
                            const label = (el.innerText || el.textContent || '').toLowerCase();
                            if (el.dataset.wawBound) return;
                            el.dataset.wawBound = '1';
                            if (label.includes('remote')) el.addEventListener('click', () => WAW.openRemote());
                            else if (label.includes('watermark')) el.addEventListener('click', () => WAW.openWatermark());
                            else if (label.includes('lokasi') || label.includes('fingerprint') || label.includes('absensi') || label.includes('pdf') || label.includes('dokumen')) el.addEventListener('click', () => WAW.openTools());
                          });
                        })();
                    """.trimIndent(), null)
                }
            }
            webChromeClient = WebChromeClient()
            addJavascriptInterface(WawDashboardBridge(), "WAW")
            loadUrl("file:///android_asset/waw-dashboard.html")
        }
        setContentView(webView)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private inner class WawDashboardBridge {
        @JavascriptInterface fun openRemote() = runOnUiThread { startActivity(Intent(this@WorkspaceActivity, com.waw.messenger.remote.RemoteHostActivity::class.java)) }
        @JavascriptInterface fun openTools() = runOnUiThread { startActivity(Intent(this@WorkspaceActivity, ToolsActivity::class.java)) }
        @JavascriptInterface fun openWatermark() = openTools()
        @JavascriptInterface fun openLocation() = openTools()
        @JavascriptInterface fun openAttendance() = openTools()
    }
}
