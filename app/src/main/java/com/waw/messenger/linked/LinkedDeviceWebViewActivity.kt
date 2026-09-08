package com.waw.messenger.linked

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
    private lateinit var headerChrome: LinearLayout
    private lateinit var bottomChrome: LinearLayout
    private lateinit var chatComposer: LinearLayout
    private var preLoginOverlay: LinearLayout? = null
    private var chromeHeaderVisible = false
    private val loginUiHandler = Handler(Looper.getMainLooper())
    private val loginUiCheck = object : Runnable {
        override fun run() {
            if (!::webView.isInitialized) return
            webView.evaluateJavascript(
                """
                (() => {
                  const text = (document.body?.innerText || '').toLowerCase();
                  const loginText = /scan to log in|pindai untuk login|use whatsapp on your computer|gunakan whatsapp di komputer|link with phone number|tautkan dengan nomor telepon/.test(text);
                  const qr = document.querySelector('[data-testid="qr-code"], canvas[aria-label*="scan" i], [aria-label*="scan to log in" i], [aria-label*="pindai" i]');
                  const ready = document.readyState === 'complete' && text.length > 40;
                  return ready && !(loginText || !!qr);
                })();
                """.trimIndent()
            ) { result ->
                val loggedIn = result == "true"
                val overlayVisible = preLoginOverlay?.visibility == View.VISIBLE
                if (loggedIn && overlayVisible) {
                    preLoginOverlay?.visibility = View.GONE
                }
                if (overlayVisible && !loggedIn) {
                    setLinkedChromeVisible(false)
                } else {
                    setLinkedChromeVisible(true, includeComposer = loggedIn)
                }
                loginUiHandler.postDelayed(this, 1000L)
            }
        }
    }
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
            setBackgroundColor(Color.rgb(247, 249, 248))
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
        addWawChrome()
        addWawChatComposer()
        addWawPreLoginOverlay()
        setLinkedChromeVisible(false)
        loginUiHandler.post(loginUiCheck)
        requestRuntimePermissionsIfNeeded()
    }

    private fun addWawChrome() {
        val green = Color.rgb(0, 150, 90)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 8, 12, 8)
            setBackgroundColor(Color.WHITE)
            val brand = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            brand.addView(ImageView(context).apply {
                setImageResource(com.waw.messenger.R.drawable.waw_main_logo)
                contentDescription = "Logo WAW"
            }, LinearLayout.LayoutParams(44, 44))
            brand.addView(TextView(context).apply {
                text = "WAW  BUSINESS"
                textSize = 18f
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
                text = "WhatsApp Workspace"
                textSize = 12.5f
                setTextColor(Color.rgb(107, 114, 128))
            })
            val tabs = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            listOf("\uf075" to "Chat", "\uf2a0" to "Panggilan", "\uf1ea" to "Status", "\uf1b3" to "Fitur", "\uf07b" to "Workspace").forEach { (icon, label) ->
                val tab = TextView(context).apply {
                    FaText.set(this, context, icon, label)
                    textSize = 12f
                    maxLines = 1
                    setTextColor(if (label.endsWith("Chat")) Color.WHITE else Color.rgb(107, 114, 128))
                    background = GradientDrawable().apply {
                        setColor(if (label.endsWith("Chat")) Color.rgb(22, 24, 28) else Color.rgb(247, 248, 250))
                        cornerRadius = 40f
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
                setTextColor(Color.rgb(14, 122, 87))
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

    private fun setLinkedChromeVisible(visible: Boolean, includeComposer: Boolean = true) {
        val state = if (visible) View.VISIBLE else View.GONE
        val headerChanged = visible != chromeHeaderVisible
        chromeHeaderVisible = visible
        if (::headerChrome.isInitialized) headerChrome.visibility = state
        if (::bottomChrome.isInitialized) bottomChrome.visibility = state
        if (::chatComposer.isInitialized) chatComposer.visibility = if (includeComposer) state else View.GONE
        if (visible && headerChanged && ::headerChrome.isInitialized) {
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
            isSingleLine = true
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

    private fun dismissPreLogin() {
        preLoginOverlay?.visibility = View.GONE
        setLinkedChromeVisible(true, includeComposer = false)
    }

    /** WAW-owned pre-login home, aligned with the waw-home UI reference. */
    private fun addWawPreLoginOverlay() {
        val d = resources.displayMetrics.density
        fun Int.dpx() = (this * d).toInt()
        val white = Color.WHITE
        val bg = Color.rgb(247, 248, 250)
        val ink = Color.rgb(17, 24, 39)
        val muted = Color.rgb(107, 114, 128)
        val line = Color.rgb(236, 238, 241)
        val g1 = Color.rgb(13, 166, 120)
        val g2 = Color.rgb(14, 142, 139)
        val greenInk = Color.rgb(14, 122, 87)
        val greenTint = Color.rgb(231, 247, 241)
        val pillDark = Color.rgb(22, 24, 28)
        val openWorkspace = { startActivity(Intent(this@LinkedDeviceWebViewActivity, WorkspaceActivity::class.java)) }

        val overlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        // Header: brand mark + WAW BUSINESS + subtitle
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(white)
            setPadding(18.dpx(), 8.dpx(), 12.dpx(), 12.dpx())
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        top.addView(TextView(this).apply {
            text = "W"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(white)
            setTypeface(typeface, Typeface.BOLD)
            background = GradientDrawable().apply {
                colors = intArrayOf(g1, g2)
                cornerRadius = 12.dpx().toFloat()
            }
        }, LinearLayout.LayoutParams(40.dpx(), 40.dpx()))
        val brandCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        val brandRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        brandRow.addView(TextView(this).apply {
            text = "WAW"
            textSize = 18f
            setTextColor(ink)
            setTypeface(typeface, Typeface.BOLD)
        })
        brandRow.addView(TextView(this).apply {
            text = "BUSINESS"
            textSize = 10f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(37, 99, 235))
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(Color.rgb(231, 240, 254)); cornerRadius = 30f }
            setPadding(14.dpx(), 3.dpx(), 14.dpx(), 3.dpx())
        }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = 8.dpx() })
        brandCol.addView(brandRow)
        brandCol.addView(TextView(this).apply { text = "WhatsApp Workspace"; textSize = 12.5f; setTextColor(muted) })
        top.addView(brandCol, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = 10.dpx() })
        top.addView(TextView(this).apply {
            textSize = 17f
            setTextColor(ink)
        }.also { FaText.set(it, this@LinkedDeviceWebViewActivity, "\uf002", "") })
        top.addView(TextView(this).apply {
            textSize = 17f
            setTextColor(ink)
            setPadding(20.dpx(), 0, 0, 0)
        }.also { FaText.set(it, this@LinkedDeviceWebViewActivity, "\uf142", "") })
        header.addView(top, LinearLayout.LayoutParams(-1, 44.dpx()))

        fun tab(label: String, active: Boolean, onClick: (() -> Unit)? = null): TextView = TextView(this).apply {
            text = label
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(if (active) white else muted)
            background = GradientDrawable().apply {
                setColor(if (active) pillDark else bg)
                cornerRadius = 60f
            }
            gravity = Gravity.CENTER
            setPadding(30.dpx(), 7.dpx(), 30.dpx(), 7.dpx())
            setOnClickListener { onClick?.invoke() }
        }
        val tabsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
        }
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 14.dpx(), 0, 0) }
        tabs.addView(tab("Chat", true))
        tabs.addView(tab("Panggilan", false))
        tabs.addView(tab("Status", false))
        tabs.addView(tab("Fitur", false))
        tabs.addView(tab("Workspace", false) { openWorkspace() })
        tabsScroll.addView(tabs, FrameLayout.LayoutParams(-2, -2))
        header.addView(tabsScroll, LinearLayout.LayoutParams(-1, -2))
        overlay.addView(header, LinearLayout.LayoutParams(-1, -2))

        // Body
        val body = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val bodyCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, 18.dpx()) }

        val sectionHead = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(18.dpx(), 18.dpx(), 18.dpx(), 0) }
        sectionHead.addView(TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(46, 99, 214))
        }.also { FaText.set(it, this@LinkedDeviceWebViewActivity, "\uf5fd", "") })
        sectionHead.addView(TextView(this).apply {
            text = "Workspace Quick Access"
            textSize = 14.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ink)
            setPadding(8.dpx(), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        sectionHead.addView(TextView(this).apply {
            text = "Lihat semua"
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(g2)
            setOnClickListener { openWorkspace() }
        })
        bodyCol.addView(sectionHead, LinearLayout.LayoutParams(-1, -2))

        fun quickCard(icon: String, title: String, sub: String, tint: Int, iconColor: Int): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(white)
                cornerRadius = 16.dpx().toFloat()
                setStroke(1, line)
            }
            setPadding(14.dpx(), 14.dpx(), 14.dpx(), 14.dpx())
            setOnClickListener { openWorkspace() }
            addView(TextView(this@LinkedDeviceWebViewActivity).apply {
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(iconColor)
                background = GradientDrawable().apply { setColor(tint); cornerRadius = 12.dpx().toFloat() }
            }.also { FaText.set(it, this@LinkedDeviceWebViewActivity, icon, "") }, LinearLayout.LayoutParams(40.dpx(), 40.dpx()))
            val col = LinearLayout(this@LinkedDeviceWebViewActivity).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(this@LinkedDeviceWebViewActivity).apply {
                text = title
                textSize = 13.5f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(ink)
            })
            col.addView(TextView(this@LinkedDeviceWebViewActivity).apply { text = sub; textSize = 12f; setTextColor(muted) })
            addView(col, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = 12.dpx() })
        }

        fun quickRow(first: LinearLayout, second: LinearLayout) = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(18.dpx(), 10.dpx(), 18.dpx(), 0)
            addView(first, LinearLayout.LayoutParams(0, -2, 1f))
            addView(second, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = 10.dpx() })
        }
        bodyCol.addView(quickRow(
            quickCard("\uf15c", "Dokumen", "12 file", Color.rgb(234, 241, 254), Color.rgb(46, 99, 214)),
            quickCard("\uf058", "Tugas", "8 aktif", greenTint, greenInk)
        ), LinearLayout.LayoutParams(-1, -2))
        bodyCol.addView(quickRow(
            quickCard("\uf133", "Kalender", "3 meeting", Color.rgb(241, 236, 254), Color.rgb(110, 66, 214)),
            quickCard("\uf07b", "File", "2,4 GB", Color.rgb(254, 241, 230), Color.rgb(196, 105, 26))
        ), LinearLayout.LayoutParams(-1, -2))

        // Connect CTA (pre-login action)
        val cta = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                colors = intArrayOf(g1, g2)
                cornerRadius = 20.dpx().toFloat()
            }
            setPadding(18.dpx(), 18.dpx(), 18.dpx(), 18.dpx())
            setOnClickListener { dismissPreLogin() }
        }
        cta.addView(TextView(this).apply {
            text = "Mulai pakai WAW"
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(white)
        })
        cta.addView(TextView(this).apply {
            text = "Hubungkan akun lewat WhatsApp Web resmi. Tanpa password, tanpa token di perangkat."
            textSize = 12.5f
            setTextColor(Color.rgb(226, 247, 241))
            setPadding(0, 4.dpx(), 0, 0)
        })
        val ctaRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 16.dpx(), 0, 0) }
        val ctaBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(white); cornerRadius = 50f }
            setPadding(16.dpx(), 9.dpx(), 16.dpx(), 9.dpx())
            addView(TextView(context).apply {
                text = "Hubungkan sekarang"
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(greenInk)
            })
            addView(TextView(context).apply {
                textSize = 13f
                setTextColor(greenInk)
                setPadding(6.dpx(), 0, 0, 0)
            }.also { FaText.set(it, this@LinkedDeviceWebViewActivity, "\uf061", "") })
        }
        ctaRow.addView(ctaBtn)
        cta.addView(ctaRow)
        val ctaWrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18.dpx(), 20.dpx(), 18.dpx(), 0) }
        ctaWrap.addView(cta)
        bodyCol.addView(ctaWrap, LinearLayout.LayoutParams(-1, -2))

        // Chats header + demo row (same as reference)
        val chatsHead = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(18.dpx(), 24.dpx(), 18.dpx(), 10.dpx()) }
        chatsHead.addView(TextView(this).apply {
            text = "OBROLAN"
            textSize = 12.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(muted)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        chatsHead.addView(TextView(this).apply {
            text = "6 \u2022 3 baru"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(greenInk)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(greenTint); cornerRadius = 60f }
            setPadding(10.dpx(), 3.dpx(), 10.dpx(), 3.dpx())
        })
        bodyCol.addView(chatsHead, LinearLayout.LayoutParams(-1, -2))
        bodyCol.addView(View(this).apply { setBackgroundColor(line) }, LinearLayout.LayoutParams(-1, 1).apply { leftMargin = 18.dpx(); rightMargin = 18.dpx() })

        val chatItem = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10.dpx(), 12.dpx(), 10.dpx(), 12.dpx())
        }
        chatItem.addView(TextView(this).apply {
            text = "CW"
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(white)
            background = GradientDrawable().apply {
                colors = intArrayOf(g1, g2)
                cornerRadius = 13.dpx().toFloat()
            }
        }, LinearLayout.LayoutParams(46.dpx(), 46.dpx()))
        val mid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        mid.addView(TextView(this).apply {
            text = "Tim WAW"
            textSize = 14.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ink)
        })
        mid.addView(TextView(this).apply {
            text = "Aku tambahin animasi: pesan masuk slide kiri..."
            textSize = 12.5f
            setTextColor(muted)
            maxLines = 1
        })
        chatItem.addView(mid, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = 12.dpx() })
        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END }
        right.addView(TextView(this).apply { text = "10:44"; textSize = 11.5f; setTextColor(muted) })
        right.addView(TextView(this).apply {
            text = "3"
            textSize = 10f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(white)
            background = GradientDrawable().apply { setColor(Color.rgb(23, 166, 115)); cornerRadius = 50f }
        }, LinearLayout.LayoutParams(18.dpx(), 18.dpx()).apply { topMargin = 6.dpx() })
        chatItem.addView(right)
        bodyCol.addView(chatItem, LinearLayout.LayoutParams(-1, -2))
        bodyCol.addView(TextView(this).apply {
            text = "Obrolan lain akan muncul di sini"
            textSize = 12.5f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 10.dpx(), 0, 10.dpx())
        }, LinearLayout.LayoutParams(-1, -2))

        body.addView(bodyCol, FrameLayout.LayoutParams(-1, -2))
        overlay.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))

        // Bottom nav
        overlay.addView(View(this).apply { setBackgroundColor(line) }, LinearLayout.LayoutParams(-1, 1))
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(white)
            setPadding(6.dpx(), 8.dpx(), 6.dpx(), 14.dpx())
        }
        fun navItem(icon: String, label: String, active: Boolean, onClick: (() -> Unit)? = null): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 2.dpx(), 0, 2.dpx())
            setOnClickListener { onClick?.invoke() }
            addView(TextView(this@LinkedDeviceWebViewActivity).apply {
                FaText.set(this, this@LinkedDeviceWebViewActivity, icon, "")
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(if (active) greenInk else muted)
            })
            addView(TextView(this@LinkedDeviceWebViewActivity).apply {
                text = label
                textSize = 11f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(if (active) greenInk else muted)
            })
        }
        bottom.addView(navItem("\uf075", "Chat", true), LinearLayout.LayoutParams(0, -2, 1f))
        bottom.addView(navItem("\uf2a0", "Panggilan", false), LinearLayout.LayoutParams(0, -2, 1f))
        bottom.addView(navItem("\uf1ea", "Status", false), LinearLayout.LayoutParams(0, -2, 1f))
        bottom.addView(navItem("\uf1b3", "Fitur", false), LinearLayout.LayoutParams(0, -2, 1f))
        bottom.addView(navItem("\uf07b", "Workspace", false) { openWorkspace() }, LinearLayout.LayoutParams(0, -2, 1f))
        overlay.addView(bottom, LinearLayout.LayoutParams(-1, -2))

        preLoginOverlay = overlay
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                setLinkedChromeVisible(false)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setLinkedChromeVisible(false)
                applyWawWebTheme()
                loginUiHandler.removeCallbacks(loginUiCheck)
                loginUiHandler.post(loginUiCheck)
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

    private fun applyWawWebTheme() {
        webView.evaluateJavascript("""
            (() => {
              const id = 'waw-production-theme';
              document.getElementById(id)?.remove();
              const style = document.createElement('style');
              style.id = id;
              style.textContent = `
                :root { color-scheme: light !important; }
                html, body, #app, body > div { background: #f7f9f8 !important; color: #18211f !important; }
                [data-testid="conversation-panel-wrapper"], [data-testid="conversation-panel-messages"], [data-testid="chatlist"] {
                  background: #f7f9f8 !important;
                }
                [data-testid="cell-frame-container"] {
                  margin: 5px 10px !important; border-radius: 16px !important;
                  border-left: 4px solid #25d366 !important; background: #ffffff !important;
                  box-shadow: 0 3px 12px rgba(18,140,126,.08) !important;
                }
                [data-testid="chat-list"] [data-testid="cell-frame-container"]:nth-child(3n) { border-left-color: #6b8cff !important; }
                [data-testid="chat-list"] [data-testid="cell-frame-container"]:nth-child(3n+1) { border-left-color: #ffb84d !important; }
                [data-testid="chat-list"] [data-testid="cell-frame-container"]:nth-child(3n+2) { border-left-color: #d28cff !important; }
                [data-testid="conversation-panel-header"] { background: #128c7e !important; color: #ffffff !important; }
                [data-testid="conversation-compose-box-input"] { background: #ffffff !important; border-radius: 24px !important; }
              `;
              document.head.appendChild(style);
            })();
        """, null)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        loginUiHandler.removeCallbacks(loginUiCheck)
        super.onDestroy()
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
