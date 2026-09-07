package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/**
 * WAW-owned UI shell.
 *
 * WAW owns navigation and presentation. The official WhatsApp component is
 * isolated in LinkedDeviceWebViewActivity for the real account/link operation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WawApp(onConnect = {
                startActivity(Intent(this, LinkedDeviceWebViewActivity::class.java))
            })
        }
    }
}

private val WawBg = Color(0xFFF4F7F7)
private val WawDark = Color(0xFF102326)
private val WawGreen = Color(0xFF10B981)
private val WawMuted = Color(0xFF718083)

private data class ChatPreview(val name: String, val message: String, val time: String, val unread: Int)

private val demoChats = listOf(
    ChatPreview("WAW Team", "Selamat datang di WAW", "08:41", 2),
    ChatPreview("Workspace", "File dan tugas Anda tersedia", "Kemarin", 0),
    ChatPreview("WhatsApp Resmi", "Buka koneksi resmi untuk chat", "", 0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WawApp(onConnect: () -> Unit) {
    Scaffold(
        containerColor = WawBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WAW", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text("Workspace", fontSize = 11.sp, color = WawMuted)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Cari") }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "Menu") }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("Chat") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Call, null) }, label = { Text("Panggilan") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Pengaturan") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ConnectionCard(onConnect)
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text("Percakapan", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WawDark, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
                }
                items(demoChats) { chat -> ChatRow(chat, onConnect) }
            }
        }
    }
}

@Composable
private fun ConnectionCard(onConnect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(20.dp)).background(WawDark).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Box(Modifier.size(46.dp).clip(CircleShape).background(WawGreen), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Link, contentDescription = null, tint = WawDark)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("WhatsApp resmi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Engine/link akun melalui WhatsApp resmi", color = Color(0xFFB7C7C8), fontSize = 12.sp)
            }
        }
        Button(onClick = onConnect, colors = ButtonDefaults.buttonColors(containerColor = WawGreen, contentColor = WawDark), shape = RoundedCornerShape(12.dp)) {
            Text("Hubungkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChatRow(chat: ChatPreview, onConnect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { if (chat.name == "WhatsApp Resmi") onConnect() }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFDCE8E7)), contentAlignment = Alignment.Center) {
            Text(chat.name.take(1), color = WawDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = WawDark)
            Text(chat.message, fontSize = 13.sp, color = WawMuted, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (chat.time.isNotBlank()) Text(chat.time, fontSize = 11.sp, color = WawMuted)
            if (chat.unread > 0) androidx.compose.foundation.layout.Box(Modifier.size(20.dp).clip(CircleShape).background(WawGreen), contentAlignment = Alignment.Center) {
                Text(chat.unread.toString(), fontSize = 10.sp, color = WawDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
