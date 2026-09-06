package com.waw.messenger.remote

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

class RemoteHostActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val manager = remember { RemoteSessionManager(this@RemoteHostActivity) }
            var code by remember { mutableStateOf(manager.currentCode()) }
            var status by remember { mutableStateOf(manager.status()) }
            var target by remember { mutableStateOf("Pilih target") }
            val projection = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    startForegroundService(Intent(this@RemoteHostActivity, ScreenShareService::class.java))
                    status = "SCREEN_SHARE_APPROVED"
                } else status = "SCREEN_SHARE_DENIED"
            }
            MaterialTheme {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Remote Control", style = MaterialTheme.typography.headlineSmall)
                    Text("Android → Windows atau Android → Android. Pairing harus disetujui kedua perangkat.", modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = { target = "Android → Windows" }, modifier = Modifier.padding(top = 16.dp)) { Text("Pilih Windows") }
                    Button(onClick = { target = "Android → Android" }, modifier = Modifier.padding(top = 8.dp)) { Text("Pilih Android lain") }
                    Text("Target: $target", modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = { code = manager.generatePairingCode(); status = manager.status() }, modifier = Modifier.padding(top = 20.dp)) { Text("Buat Kode Pairing") }
                    if (code.isNotBlank()) Text("Kode sekali pakai: $code", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                    Text("Status: $status", modifier = Modifier.padding(top = 12.dp))
                    Button(onClick = {
                        val service = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        projection.launch(service.createScreenCaptureIntent())
                    }, modifier = Modifier.padding(top = 20.dp)) { Text("Izinkan Screen Share") }
                    Button(onClick = { manager.revoke(); stopService(Intent(this@RemoteHostActivity, ScreenShareService::class.java)); code = ""; status = manager.status() }, modifier = Modifier.padding(top = 8.dp)) { Text("Revoke / Putuskan Pairing") }
                }
            }
        }
    }
}
