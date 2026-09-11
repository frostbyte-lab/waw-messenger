package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.waw.messenger.linked.LinkedDeviceWebViewActivity
import com.waw.messenger.remote.RemoteHostActivity

private val WawInk = Color(0xFF10201C)
private val WawGreen = Color(0xFF17B978)
private val WawMint = Color(0xFFE7F7EF)
private val WawCanvas = Color(0xFFF6F8F7)
private val WawMuted = Color(0xFF71817B)
private val WawBusiness = Color(0xFF277A57)

class WorkspaceActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WawHybridApp() }
    }

    @Composable
    private fun WawHybridApp() {
        var selected by remember { mutableIntStateOf(0) }
        Scaffold(
            containerColor = WawCanvas,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf(
                        Triple("Chat", Icons.Rounded.ChatBubble, "Chat internal"),
                        Triple("Panggilan", Icons.Rounded.Phone, "Panggilan internal"),
                        Triple("Status", Icons.Rounded.NotificationsNone, "Status WAW"),
                        Triple("Fitur", Icons.Rounded.MoreHoriz, "Fitur WAW"),
                        Triple("Workspace", Icons.Rounded.Folder, "Workspace")
                    ).forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            icon = { Icon(item.second, contentDescription = item.third) },
                            label = { Text(item.first, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                0 -> InternalHome(padding)
                1 -> EmptySection(padding, "Panggilan internal", "Panggilan WebRTC WAW dengan consent kamera dan mikrofon.", Icons.Rounded.Phone)
                2 -> EmptySection(padding, "Status WAW", "Bagikan pembaruan hanya kepada anggota workspace yang Anda pilih.", Icons.Rounded.NotificationsNone)
                3 -> Features(padding)
                else -> Workspace(padding)
            }
        }
    }

    @Composable
    private fun InternalHome(padding: PaddingValues) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { InternalHero() }
            item { SectionTitle("Percakapan internal", "WAW Internal") }
            items(listOf("Tim Support" to "Ayo selesaikan tiket workspace hari ini", "Operator" to "Sesi remote sudah dicabut", "Workspace Team" to "3 dokumen baru tersedia")) { chat -> ChatRow(chat.first, chat.second) }
            item { SectionTitle("Channel resmi", "Terpisah") }
            item { BusinessChannel() }
        }
    }

    @Composable
    private fun Header() {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Image(painterResource(R.drawable.waw_main_logo), "Logo WAW", Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("WAW", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WawInk)
                Text("INTERNAL WORKSPACE", fontSize = 10.sp, letterSpacing = 1.4.sp, color = WawGreen, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}) { Icon(Icons.Rounded.Settings, "Pengaturan", tint = WawMuted) }
        }
    }

    @Composable
    private fun InternalHero() {
        Card(colors = CardDefaults.cardColors(containerColor = WawInk), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Shield, null, tint = WawGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("WAW INTERNAL", color = WawGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.weight(1f))
                    Text("● ONLINE", color = Color(0xFF9BE9C3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("Selamat datang di ruang kerja Anda", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Chat, status, remote, dan dokumen WAW berada di sini. Data internal tidak dicampur dengan channel Business.", color = Color(0xFFB5C9C1), fontSize = 13.sp, lineHeight = 19.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = {}, colors = ButtonDefaults.filledTonalButtonColors(containerColor = WawGreen, contentColor = WawInk)) { Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(4.dp)); Text("Chat baru") }
                    TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("Lihat status") }
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(title: String, badge: String) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = WawInk, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(badge, color = WawBusiness, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    private fun ChatRow(name: String, message: String) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(14.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(WawMint), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Groups, null, tint = WawBusiness) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(name, color = WawInk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp); Text(message, color = WawMuted, fontSize = 12.sp, maxLines = 1) }
            Text("09:41", color = WawMuted, fontSize = 10.sp)
        }
    }

    @Composable
    private fun BusinessChannel() {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Business, null, tint = WawBusiness); Spacer(Modifier.width(9.dp)); Text("WhatsApp Business", color = WawInk, fontWeight = FontWeight.Bold, fontSize = 16.sp); Spacer(Modifier.weight(1f)); Text("CHANNEL TERPISAH", color = WawBusiness, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                Text("Gunakan layanan resmi setelah Anda memilih dan menyetujui. Chat personal tidak disimpan di WAW.", color = WawMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Button(onClick = { startActivity(Intent(this@WorkspaceActivity, LinkedDeviceWebViewActivity::class.java)) }, colors = ButtonDefaults.buttonColors(containerColor = WawBusiness), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) { Text("Buka WhatsApp Business resmi") }
            }
        }
    }

    @Composable
    private fun Features(padding: PaddingValues) { EmptySection(padding, "Fitur WAW", "Remote, file, scanner, dokumen, dan keamanan workspace.", Icons.Rounded.Devices, true) }

    @Composable
    private fun Workspace(padding: PaddingValues) { EmptySection(padding, "Workspace", "Semua tools internal WAW berada dalam satu ruang kerja.", Icons.Rounded.Folder, true) }

    @Composable
    private fun EmptySection(padding: PaddingValues, title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector, actions: Boolean = false) {
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Header()
            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(WawMint).padding(28.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, null, tint = WawBusiness, modifier = Modifier.size(42.dp))
                    Text(title, color = WawInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(body, color = WawMuted, fontSize = 13.sp, lineHeight = 19.sp)
                    if (actions) {
                        Button(onClick = { startActivity(Intent(this@WorkspaceActivity, ToolsActivity::class.java)) }, colors = ButtonDefaults.buttonColors(containerColor = WawBusiness)) { Text("Buka tools WAW") }
                        FilledTonalButton(onClick = { startActivity(Intent(this@WorkspaceActivity, RemoteHostActivity::class.java)) }) { Icon(Icons.Rounded.Devices, null); Spacer(Modifier.width(6.dp)); Text("Workspace Remote") }
                    }
                }
            }
        }
    }
}
