package com.waw.messenger.remote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity

class RemoteHostActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RemoteUserScreen() }
    }

    @Composable
    private fun RemoteUserScreen() {
        val context = this@RemoteHostActivity
        val manager = remember { RemoteSessionManager(context) }
        var code by remember { mutableStateOf(manager.currentCode()) }
        var status by remember { mutableStateOf(manager.status()) }
        var relayUrl by remember { mutableStateOf("") }
        val clipboard = LocalClipboardManager.current
        val projection = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                if (!relayUrl.startsWith("wss://")) {
                    status = "RELAY_URL_REQUIRED"
                    return@rememberLauncherForActivityResult
                }
                val serviceIntent = Intent(context, ScreenShareService::class.java).apply {
                    putExtra(ScreenShareService.EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(ScreenShareService.EXTRA_RESULT_DATA, result.data)
                    putExtra(ScreenShareService.EXTRA_RELAY_URL, relayUrl.trim())
                }
                androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
                status = "SCREEN_SHARE_ACTIVE"
            } else status = "SCREEN_SHARE_DENIED"
        }

        Surface(color = Ink) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF15372D), Ink, Ink)))) {
                Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).background(Green, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ScreenShare, null, tint = Ink, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("WAW Remote User", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Perangkat Anda tetap memegang kendali", color = Muted, fontSize = 13.sp)
                        }
                        UserStatus(status)
                    }

                    Text("Bagikan akses dengan aman", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Text("Buat OTP satu kali, lalu izinkan screen share hanya ketika Anda siap. Anda dapat menghentikan sesi kapan saja.", color = Muted, lineHeight = 20.sp)

                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Key, null, tint = Green)
                                Spacer(Modifier.width(8.dp))
                                Text("OTP PAIRING", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Rounded.Security, null, tint = Muted, modifier = Modifier.size(18.dp))
                            }
                            Box(Modifier.fillMaxWidth().background(Color(0xFF071713), RoundedCornerShape(16.dp)).padding(vertical = 19.dp), contentAlignment = Alignment.Center) {
                                Text(if (code.isBlank()) "— — — — — —" else code.chunked(1).joinToString(" "), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = { code = manager.generatePairingCode(); status = manager.status() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink), shape = RoundedCornerShape(13.dp)) { Text("Buat OTP", fontWeight = FontWeight.Bold) }
                                TextButton(onClick = { if (code.isNotBlank()) clipboard.setText(AnnotatedString(code)) }, enabled = code.isNotBlank()) { Icon(Icons.Rounded.ContentCopy, null); Spacer(Modifier.width(4.dp)); Text("Salin") }
                            }
                            Text("OTP tersimpan lokal dan kedaluwarsa setelah waktu pairing berakhir.", color = Muted, fontSize = 11.sp)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10231F)), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("SESSION SETUP", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            OutlinedTextField(value = relayUrl, onValueChange = { relayUrl = it }, singleLine = true, leadingIcon = { Icon(Icons.Rounded.Link, null, tint = Green) }, label = { Text("Relay URL wss://") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = {
                                val service = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                                projection.launch(service.createScreenCaptureIntent())
                            }, enabled = code.length == 6 && relayUrl.startsWith("wss://"), modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A6B55)), shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Rounded.ScreenShare, null); Spacer(Modifier.width(8.dp)); Text("Izinkan & mulai remote", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                                Icon(Icons.Rounded.Lock, null, tint = Green); Spacer(Modifier.width(6.dp)); Text("Aktifkan kontrol sentuh (opsional)", color = Green)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, null, tint = Muted, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text("Tidak ada akses tanpa OTP + persetujuan screen share.", color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { manager.revoke(); stopService(Intent(context, ScreenShareService::class.java)); code = ""; status = manager.status() }) { Icon(Icons.Rounded.PowerSettingsNew, null, tint = Color(0xFFF2798B)); Spacer(Modifier.width(4.dp)); Text("Putuskan", color = Color(0xFFF2798B)) }
                    }
                }
            }
        }
    }
}

private val Ink = Color(0xFF07110F)
private val Panel = Color(0xFF10231F)
private val Green = Color(0xFF20D486)
private val Muted = Color(0xFF9AB5AC)

@Composable
private fun UserStatus(status: String) {
    val active = status == "SCREEN_SHARE_ACTIVE"
    Box(Modifier.background(if (active) Color(0x3320D486) else Color(0x332F4D45), RoundedCornerShape(50))) {
        Text(if (active) "SHARING" else status.replace('_', ' '), color = if (active) Green else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
    }
}
