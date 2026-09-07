package com.waw.userremote.workspace

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { RemoteConsentScreen() }; if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1001) }
    @Composable private fun RemoteConsentScreen() {
        val items = listOf("SCREEN_SHARE" to "Melihat layar perangkat", "TOUCH_INPUT" to "Mengirim tap dan swipe", "KEYBOARD_INPUT" to "Tombol navigasi dan text input", "FILE_TRANSFER" to "Transfer file melalui picker eksplisit", "APPROVED_ACTIONS" to "Actions aman: Back, Home, Recents, notifikasi", "APP_ACCESS" to "Membuka aplikasi setelah persetujuan per permintaan")
        val checked = remember { mutableStateMapOf<String, Boolean>() }
        var relayUrl by remember { mutableStateOf(intent?.data?.getQueryParameter("relay").orEmpty()) }
        var code by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("READY") }
        var pendingOffer by remember { mutableStateOf<JSONObject?>(null) }
        var pendingApp by remember { mutableStateOf<JSONObject?>(null) }
        val manager = remember { RemoteSessionManager(this@MainActivity) }
        val allChecked = items.all { checked[it.first] == true }
        val connectionPulse by rememberInfiniteTransition(label = "connection").animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse), label = "connectionPulse")
        val movingDot by rememberInfiniteTransition(label = "data-flow").animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Restart), label = "movingDot")
        val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
            val offer = pendingOffer; pendingOffer = null
            if (uri != null && offer != null) runCatching {
                val bytes = Base64.decode(offer.getString("payloadBase64"), Base64.DEFAULT)
                val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                check(digest == offer.optString("sha256")) { "checksum mismatch" }
                contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("cannot open destination")
                state = "FILE_SAVED"
            }.onFailure { state = "FILE_SAVE_FAILED" }
        }
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ScreenShareService.ACTION_STATE) state = intent.getStringExtra(ScreenShareService.EXTRA_STATE).orEmpty().ifBlank { state }
                    if (intent.action == ScreenShareService.ACTION_FILE_OFFER) pendingOffer = runCatching { JSONObject(intent.getStringExtra(ScreenShareService.EXTRA_FILE_OFFER).orEmpty()) }.getOrNull()
                    if (intent.action == ScreenShareService.ACTION_APP_REQUEST) pendingApp = runCatching { JSONObject(intent.getStringExtra(ScreenShareService.EXTRA_APP_REQUEST).orEmpty()) }.getOrNull()
                }
            }
            registerReceiver(receiver, IntentFilter().apply { addAction(ScreenShareService.ACTION_STATE); addAction(ScreenShareService.ACTION_FILE_OFFER); addAction(ScreenShareService.ACTION_APP_REQUEST) }, Context.RECEIVER_NOT_EXPORTED)
            onDispose { unregisterReceiver(receiver) }
        }
        val projection = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK || result.data == null) { state = "SCREEN_PERMISSION_DENIED"; return@rememberLauncherForActivityResult }
            val caps = items.filter { checked[it.first] == true }.map { it.first }
            startForegroundService(Intent(this@MainActivity, ScreenShareService::class.java).apply {
                putExtra(ScreenShareService.EXTRA_CODE, code); putExtra(ScreenShareService.EXTRA_RELAY_URL, relayUrl.trim()); putStringArrayListExtra(ScreenShareService.EXTRA_CAPABILITIES, ArrayList(caps)); putExtra(ScreenShareService.EXTRA_RESULT_CODE, result.resultCode); putExtra(ScreenShareService.EXTRA_RESULT_DATA, result.data)
            })
            RemoteInputService.activate(caps.contains("TOUCH_INPUT") || caps.contains("KEYBOARD_INPUT")); state = "WAITING_FOR_OPERATOR"
        }
        Surface(color = Color(0xFF07110F), modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1D19)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().height(152.dp)) {
                        androidx.compose.foundation.Image(painterResource(com.waw.messenger.R.drawable.waw_remote_logo), contentDescription = "User Remote connection", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Canvas(Modifier.fillMaxSize()) {
                            val y = size.height * 0.51f
                            val left = size.width * 0.28f
                            val right = size.width * 0.72f
                            drawLine(Color(0xFF6DE7C1).copy(alpha = 0.35f), androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 3.dp.toPx(), cap = StrokeCap.Round)
                            drawCircle(Color(0xFFB9FFE9).copy(alpha = connectionPulse), 8.dp.toPx(), androidx.compose.ui.geometry.Offset(left + (right - left) * movingDot, y))
                            drawCircle(Color(0xFF20D486).copy(alpha = 0.2f + connectionPulse * 0.2f), (16.dp.toPx() + connectionPulse * 8.dp.toPx()), androidx.compose.ui.geometry.Offset(size.width / 2f, y), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
                Text("USER REMOTE WORKSPACE", color = Color(0xFFFFA44A), fontSize = 12.sp)
                Text("Persetujuan akses remote", color = Color.White, fontSize = 28.sp)
                Text("Tidak ada akses sebelum User menyetujui, MediaProjection diberikan, dan Operator menyetujui sesi.", color = Color(0xFFA7BBB3))
                OutlinedTextField(relayUrl, { relayUrl = it }, label = { Text("Relay URL wss://") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("OTP pairing") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10231F)), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(10.dp)) { items.forEach { (key, label) -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Checkbox(checked[key] == true, { checked[key] = it }); Text(label, color = Color.White) } } } }
                Text("Status: ${state.replace('_', ' ')}", color = Color(0xFFFFA44A))
                pendingOffer?.let { offer ->
                    AlertDialog(onDismissRequest = { pendingOffer = null }, title = { Text("File transfer") }, text = { Text("Operator mengirim ${offer.optString("name", "file")} (${offer.optLong("size")} bytes). Simpan hanya jika Anda mengenal sumbernya.") }, confirmButton = { TextButton(onClick = { saveFile.launch(offer.optString("name", "remote-file.bin")) }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = { pendingOffer = null }) { Text("Tolak") } })
                }
                pendingApp?.let { request ->
                    val label = request.optString("label", request.optString("packageName", "aplikasi"))
                    val packageName = request.optString("packageName")
                    val requestId = request.optString("requestId")
                    fun decide(allow: Boolean) {
                        startService(Intent(this@MainActivity, ScreenShareService::class.java).apply { action = ScreenShareService.ACTION_APP_DECISION; putExtra(ScreenShareService.EXTRA_REQUEST_ID, requestId); putExtra(ScreenShareService.EXTRA_PACKAGE_NAME, packageName); putExtra(ScreenShareService.EXTRA_APPROVED, allow); putExtra(ScreenShareService.EXTRA_REASON, if (allow) "user approved" else "user denied") })
                        if (allow) packageManager.getLaunchIntentForPackage(packageName)?.let { launch: Intent -> launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); state = "APP_ACTIVE" } else state = "APP_REQUEST_DENIED"
                        pendingApp = null
                    }
                    AlertDialog(onDismissRequest = { decide(false) }, title = { Text("Permintaan akses aplikasi") }, text = { Text("Operator meminta membuka:\n$label\n\nTujuan:\nMembantu memeriksa atau mengatur aplikasi.\n\nAkses yang diminta:\n• Membuka $label\n• Mengirim navigasi setelah Anda menyetujui\n• Tidak membaca atau mengambil data tanpa persetujuan tambahan") }, confirmButton = { TextButton(onClick = { decide(true) }) { Text("✓ Izinkan") } }, dismissButton = { TextButton(onClick = { decide(false) }) { Text("× Tolak") } })
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { code = manager.generatePairingCode(); state = "WAITING_FOR_OPERATOR" }, enabled = code.isBlank(), modifier = Modifier.fillMaxWidth()) { Text("BUAT OTP SEKALI PAKAI") }
                Button(onClick = { val m = getSystemService(MediaProjectionManager::class.java); projection.launch(m.createScreenCaptureIntent()) }, enabled = allChecked && code.length == 6 && relayUrl.startsWith("wss://") && state != "ACTIVE", modifier = Modifier.fillMaxWidth()) { Text("SETUJUI & MULAI SESI") }
                TextButton(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Aktifkan Accessibility untuk input (opsional)") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }, modifier = Modifier.weight(1f)) { Text("Info aplikasi", fontSize = 12.sp) }
                    OutlinedButton(onClick = { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)) }, modifier = Modifier.weight(1f)) { Text("Izin notifikasi", fontSize = 12.sp) }
                }
                if (state == "ACTIVE") {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF17241F)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("REMOTE SESSION ACTIVE", color = Color(0xFF20D486), fontSize = 12.sp)
                            Text("Screen, input, file, dan approved actions sedang tersedia untuk Operator yang disetujui.", color = Color(0xFFA7BBB3), fontSize = 12.sp)
                            Button(onClick = { manager.revoke(); stopService(Intent(this@MainActivity, ScreenShareService::class.java)); RemoteInputService.activate(false); code = ""; state = "REVOKED" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3043)), modifier = Modifier.fillMaxWidth()) { Text("REVOKE SESSION") }
                        }
                    }
                }
            }
        }
    }
}
