package com.waw.messenger

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/**
 * WAW Hybrid native launcher.
 * WhatsApp Web is never opened automatically; it is an explicit Business action.
 */
class WorkspaceActivity : FragmentActivity() {
    private val ink = Color.rgb(20, 32, 29)
    private val green = Color.rgb(18, 199, 133)
    private val muted = Color.rgb(96, 111, 105)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 250, 248))
        }
        content.addView(header(), LinearLayout.LayoutParams(-1, -2))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 22, 20, 28)
                addView(sectionLabel("WAW HYBRID"))
                addView(title("Satu ruang kerja untuk komunikasi dan remote"))
                addView(description("Data internal WAW dan WhatsApp Business tetap dipisahkan. Pilih layanan secara sadar dari menu di bawah."))
                addView(channelCard("WAW Internal", "Chat, Status, presence, Workspace, dan remote", "waw_internal", green) { openInternal() })
                addView(channelCard("WhatsApp Business", "Buka layanan resmi Meta/WhatsApp Web hanya setelah Anda memilihnya", "whatsapp_business", Color.rgb(37, 211, 102)) { openWhatsApp() })
                addView(sectionLabel("FITUR UTAMA"))
                addView(actionCard("Workspace", "Dokumen, file, watermark, scanner, vault, dan tools", "Buka Workspace") { openTools() })
                addView(actionCard("Workspace Remote", "Sesi remote hanya berjalan setelah consent dan dapat dicabut", "Buka Remote") { openRemote() })
                addView(infoCard())
            })
        }
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(bottomBar())
        setContentView(content)
    }

    private fun header(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(20, 18, 20, 16)
        setBackgroundColor(Color.WHITE)
        addView(ImageView(context).apply {
            setImageResource(R.drawable.waw_main_logo)
            contentDescription = "Logo WAW"
        }, LinearLayout.LayoutParams(52, 52))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 0, 0, 0)
            addView(TextView(context).apply { text = "WAW"; textSize = 25f; setTextColor(ink); setTypeface(typeface, Typeface.BOLD) })
            addView(TextView(context).apply { text = "HYBRID WORKSPACE"; textSize = 10f; setTextColor(green); setTypeface(typeface, Typeface.BOLD) })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(TextView(context).apply { text = "● ONLINE"; textSize = 10f; setTextColor(green); setTypeface(typeface, Typeface.BOLD) })
    }

    private fun sectionLabel(value: String) = TextView(this).apply {
        text = value; textSize = 10f; setTextColor(green); setTypeface(typeface, Typeface.BOLD); letterSpacing = 0.12f
        setPadding(0, 8, 0, 8)
    }
    private fun title(value: String) = TextView(this).apply { text = value; textSize = 24f; setTextColor(ink); setTypeface(typeface, Typeface.BOLD); setPadding(0, 0, 0, 6) }
    private fun description(value: String) = TextView(this).apply { text = value; textSize = 13f; setTextColor(muted); setPadding(0, 0, 0, 18) }

    private fun channelCard(title: String, body: String, badge: String, color: Int, click: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(18, 16, 18, 16); setBackgroundColor(Color.WHITE); elevation = 3f; setOnClickListener { click() }
        addView(LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply { text = title; textSize = 17f; setTextColor(ink); setTypeface(typeface, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(TextView(context).apply { text = badge; textSize = 9f; setTextColor(color); setTypeface(typeface, Typeface.BOLD) })
        })
        addView(TextView(context).apply { text = body; textSize = 12f; setTextColor(muted); setPadding(0, 7, 0, 0) })
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 12) }
    }

    private fun actionCard(title: String, body: String, action: String, click: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(16, 15, 16, 15); setBackgroundColor(Color.WHITE); setOnClickListener { click() }
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = title; textSize = 15f; setTextColor(ink); setTypeface(typeface, Typeface.BOLD) })
            addView(TextView(context).apply { text = body; textSize = 11f; setTextColor(muted); setPadding(0, 4, 0, 0) })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(TextView(context).apply { text = "›"; textSize = 28f; setTextColor(green) })
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 10) }
    }

    private fun infoCard() = TextView(this).apply {
        text = "Privasi: WAW tidak menyimpan QR, cookie, password, atau session WhatsApp personal. Remote selalu membutuhkan persetujuan target dan tombol revoke."
        textSize = 11f; setTextColor(muted); setPadding(15, 14, 15, 14); setBackgroundColor(Color.rgb(232, 247, 239))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 8, 0, 0) }
    }

    private fun bottomBar() = TextView(this).apply {
        text = "Chat Internal     •     Status     •     Fitur     •     Workspace"
        gravity = Gravity.CENTER; textSize = 11f; setTextColor(muted); setPadding(12, 16, 12, 16); setBackgroundColor(Color.WHITE)
    }

    private fun openInternal() = toast("Chat internal WAW siap dihubungkan ke backend realtime")
    private fun openWhatsApp() = startActivity(Intent(this, LinkedDeviceWebViewActivity::class.java))
    private fun openRemote() = startActivity(Intent(this, remote.RemoteHostActivity::class.java))
    private fun openTools() = startActivity(Intent(this, ToolsActivity::class.java))
    private fun toast(message: String) = android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}
