package com.waw.userremote.workspace

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RemoteConsentScreen() }
    }
}

private data class ConsentItem(val key: String, val title: String, val detail: String)

@Composable
private fun RemoteConsentScreen() {
    val items = listOf(
        ConsentItem("screen", "Melihat layar perangkat", "Operator dapat melihat layar saat sesi aktif."),
        ConsentItem("input", "Mengirim tap dan input", "Operator dapat mengirim tap, swipe, dan tombol yang Anda izinkan."),
        ConsentItem("files", "Transfer file", "Operator dapat mengirim atau mengambil file yang Anda pilih."),
        ConsentItem("actions", "Menjalankan tindakan yang disetujui", "Hanya tindakan dalam daftar aman dan tercatat yang dapat dijalankan.")
    )
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    var approved by remember { mutableStateOf(false) }
    val allChecked = items.all { checked[it.key] == true }
    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(Orange, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ScreenShare, null, tint = Ink, modifier = Modifier.size(27.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("USER REMOTE WORKSPACE", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Text("Workspace Anda tetap terkendali", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Rounded.Security, "Keamanan", tint = Muted)
            }
            Spacer(Modifier.height(8.dp))
            Text("Persetujuan akses remote", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Periksa bagian yang akan diremote. Tidak ada akses berjalan sebelum Anda menyetujui dan menekan tombol ✓.", color = Muted, lineHeight = 20.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = checked[item.key] == true, onCheckedChange = { checked[item.key] = it })
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(item.detail, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF17241F)), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, null, tint = Orange)
                    Spacer(Modifier.size(10.dp))
                    Text("Sesi terlihat melalui notifikasi permanen. Tombol REVOKE selalu tersedia untuk menghentikan akses.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { approved = true }, enabled = allChecked && !approved,
                modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Ink)
            ) {
                Icon(Icons.Rounded.Check, null); Spacer(Modifier.size(8.dp)); Text(if (approved) "PERSETUJUAN TERSIMPAN" else "✓ SAYA SETUJU & LANJUTKAN", fontWeight = FontWeight.Bold)
            }
            Text(if (approved) "Menunggu pairing dan persetujuan operator." else "Centang semua bagian yang akan diremote untuk melanjutkan.", color = if (approved) Orange else Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

private val Ink = Color(0xFF07110F)
private val Panel = Color(0xFF10231F)
private val Orange = Color(0xFFFFA44A)
private val Muted = Color(0xFFA7BBB3)
