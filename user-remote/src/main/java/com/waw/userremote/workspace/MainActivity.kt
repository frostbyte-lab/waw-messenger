package com.waw.userremote.workspace

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { RemoteConsentScreen() } }
    @Composable private fun RemoteConsentScreen() {
        val items = listOf("SCREEN_SHARE" to "Melihat layar perangkat", "TOUCH_INPUT" to "Mengirim tap dan swipe", "KEYBOARD_INPUT" to "Tombol navigasi yang diizinkan", "FILE_TRANSFER" to "Transfer file melalui picker eksplisit")
        val checked = remember { mutableStateMapOf<String, Boolean>() }
        var relayUrl by remember { mutableStateOf(intent?.data?.getQueryParameter("relay").orEmpty()) }
        var code by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("READY") }
        val manager = remember { RemoteSessionManager(this@MainActivity) }
        val allChecked = items.all { checked[it.first] == true }
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
                Text("USER REMOTE WORKSPACE", color = Color(0xFFFFA44A), fontSize = 12.sp)
                Text("Persetujuan akses remote", color = Color.White, fontSize = 28.sp)
                Text("Tidak ada akses sebelum User menyetujui, MediaProjection diberikan, dan Operator menyetujui sesi.", color = Color(0xFFA7BBB3))
                OutlinedTextField(relayUrl, { relayUrl = it }, label = { Text("Relay URL wss://") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("OTP pairing") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10231F)), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(10.dp)) { items.forEach { (key, label) -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Checkbox(checked[key] == true, { checked[key] = it }); Text(label, color = Color.White) } } } }
                Text("Status: ${state.replace('_', ' ')}", color = Color(0xFFFFA44A))
                Spacer(Modifier.weight(1f))
                Button(onClick = { code = manager.generatePairingCode(); state = "WAITING_FOR_OPERATOR" }, enabled = code.isBlank(), modifier = Modifier.fillMaxWidth()) { Text("BUAT OTP SEKALI PAKAI") }
                Button(onClick = { val m = getSystemService(MediaProjectionManager::class.java); projection.launch(m.createScreenCaptureIntent()) }, enabled = allChecked && code.length == 6 && relayUrl.startsWith("wss://") && state != "ACTIVE", modifier = Modifier.fillMaxWidth()) { Text("SETUJUI & MULAI SESI") }
                TextButton(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Aktifkan Accessibility untuk input (opsional)") }
                Button(onClick = { manager.revoke(); stopService(Intent(this@MainActivity, ScreenShareService::class.java)); RemoteInputService.activate(false); code = ""; state = "REVOKED" }, enabled = state != "READY" && state != "REVOKED", colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3043)), modifier = Modifier.fillMaxWidth()) { Text("REVOKE ACCESS") }
            }
        }
    }
}
