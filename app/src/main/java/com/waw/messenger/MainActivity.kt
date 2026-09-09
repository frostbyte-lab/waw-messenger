package com.waw.messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/** WAW-owned UI shell. WhatsApp communication stays in the official component. */
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

private val availableChats = listOf(
    ChatPreview("WhatsApp Web resmi", "Hubungkan untuk membuka percakapan WhatsApp", "", 0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WawApp(onConnect: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSearchInfo by remember { mutableStateOf(false) }
    var showMenuInfo by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showSearchInfo = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                    IconButton(onClick = { showMenuInfo = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Panggilan") },
                    label = { Text("Panggilan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> ChatHome(
                modifier = Modifier.fillMaxSize().padding(padding),
                chats = availableChats,
                onConnect = onConnect
            )
            1 -> EmptySection(
                title = "Panggilan",
                message = "Panggilan akan tersedia melalui WhatsApp Web resmi setelah akun terhubung.",
                modifier = Modifier.fillMaxSize().padding(padding),
                actionLabel = "Hubungkan WhatsApp",
                onAction = onConnect
            )
            else -> EmptySection(
                title = "Pengaturan",
                message = "Pengaturan WAW akan tersedia di sini. Credential WhatsApp tidak disimpan oleh WAW.",
                modifier = Modifier.fillMaxSize().padding(padding),
                actionLabel = null,
                onAction = {}
            )
        }
    }

    if (showSearchInfo) {
        AlertDialog(
            onDismissRequest = { showSearchInfo = false },
            title = { Text("Cari") },
            text = { Text("Pencarian chat dilakukan setelah WhatsApp Web resmi terhubung.") },
            confirmButton = { TextButton(onClick = { showSearchInfo = false }) { Text("Tutup") } }
        )
    }
    if (showMenuInfo) {
        AlertDialog(
            onDismissRequest = { showMenuInfo = false },
            title = { Text("Menu WAW") },
            text = { Text("Gunakan tombol Hubungkan untuk membuka WhatsApp Web resmi. WAW tidak membaca atau menyalin session WhatsApp.") },
            confirmButton = { TextButton(onClick = { showMenuInfo = false }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun ChatHome(modifier: Modifier, chats: List<ChatPreview>, onConnect: () -> Unit) {
    Column(modifier) {
        ConnectionCard(onConnect)
        Text(
            "Percakapan",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = WawDark,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(chats) { chat -> ChatRow(chat, onConnect) }
        }
    }
}

@Composable
private fun EmptySection(
    title: String,
    message: String,
    modifier: Modifier,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WawDark)
        Text(message, color = WawMuted, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
        if (actionLabel != null) {
            Button(onClick = onAction) { Text(actionLabel) }
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
            Box(Modifier.size(46.dp).clip(CircleShape).background(WawGreen), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Link, contentDescription = null, tint = WawDark)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("WhatsApp resmi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Hubungkan melalui WhatsApp Web resmi", color = Color(0xFFB7C7C8), fontSize = 12.sp)
            }
        }
        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = WawGreen, contentColor = WawDark),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Hubungkan", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ChatRow(chat: ChatPreview, onConnect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onConnect).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFDCE8E7)), contentAlignment = Alignment.Center) {
            Text("W", color = WawDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = WawDark)
            Text(chat.message, fontSize = 13.sp, color = WawMuted, maxLines = 1)
        }
        if (chat.unread > 0) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(WawGreen), contentAlignment = Alignment.Center) {
                Text(chat.unread.toString(), fontSize = 10.sp, color = WawDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
