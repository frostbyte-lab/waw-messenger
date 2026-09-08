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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/**
 * WAW presentation shell. WhatsApp account operations remain isolated in the
 * official connection component opened by LinkedDeviceWebViewActivity.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var update by remember { mutableStateOf<WawUpdate?>(null) }
            var showUpdate by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                update = UpdateManager.findUpdate()
            }
            WawApp(hasUpdate = update != null, onConnect = {
                startActivity(Intent(this, LinkedDeviceWebViewActivity::class.java))
            })
            if (showUpdate && update != null) {
                val available = update!!
                AlertDialog(
                    onDismissRequest = { showUpdate = false },
                    title = { Text("Update WAW tersedia") },
                    text = { Text("Versi ${available.version} sudah tersedia. Unduh APK resmi dari GitHub sekarang?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showUpdate = false
                            update = null
                            UpdateManager.downloadAndInstall(this@MainActivity, available)
                        }) { Text("Update sekarang") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUpdate = false }) { Text("Nanti") }
                    }
                )
            }
        }
    }
}

private val WawGreen = Color(0xFF128C7E)
private val WawBrightGreen = Color(0xFF25D366)
private val WawBg = Color(0xFFF7F9F8)
private val WawDark = Color(0xFF18211F)
private val WawMuted = Color(0xFF71807B)
private val WawLine = Color(0xFFE2E9E6)
private val WawGlow = Color(0xFF4DFFE2)

private data class ChatPreview(
    val name: String,
    val message: String,
    val time: String,
    val unread: Int,
    val accent: Color,
    val icon: ImageVector,
)

private val workspaceChats = listOf(
    ChatPreview("Workspace", "Kelola percakapan tim dan assignment", "", 0, Color(0xFF0B8F7C), Icons.Default.Groups),
    ChatPreview("WhatsApp resmi", "Hubungkan akun untuk memuat chat bisnis", "", 0, Color(0xFF25D366), Icons.Default.Wifi),
)

@Composable
private fun GlowingIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color = WawDark,
    glow: Color = WawGlow,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(12.dp, CircleShape, ambientColor = glow.copy(alpha = 0.55f), spotColor = glow.copy(alpha = 0.8f))
            .clip(CircleShape)
            .background(glow.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WawApp(hasUpdate: Boolean, onConnect: () -> Unit) {
    Scaffold(
        containerColor = WawBg,
        topBar = {
            TopAppBar(
                title = { Text("WAW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 23.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WawGreen),
                actions = {
                    IconButton(onClick = {}) { GlowingIcon(Icons.Default.QrCodeScanner, "QR", tint = Color.White, glow = WawBrightGreen, size = 36.dp) }
                    IconButton(onClick = {}) { GlowingIcon(Icons.Default.Search, "Cari", tint = Color.White, glow = WawBrightGreen, size = 36.dp) }
                    IconButton(onClick = {}) { GlowingIcon(Icons.Default.MoreVert, "Menu", tint = Color.White, glow = WawBrightGreen, size = 36.dp) }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
                NavigationBarItem(selected = true, onClick = {}, icon = { GlowingIcon(Icons.Default.Chat, "Chat", tint = WawDark, glow = WawBrightGreen, size = 34.dp) }, label = { Text("Chat") })
                if (hasUpdate) {
                    NavigationBarItem(selected = false, onClick = {}, icon = { GlowingIcon(Icons.Default.Update, "Pembaruan tersedia", glow = Color(0xFF6B8CFF), size = 34.dp) }, label = { Text("Update") })
                }
                NavigationBarItem(selected = false, onClick = {}, icon = { GlowingIcon(Icons.Default.Call, "Panggilan", glow = Color(0xFFFFB84D), size = 34.dp) }, label = { Text("Panggilan") })
                NavigationBarItem(selected = false, onClick = {}, icon = { GlowingIcon(Icons.Default.Settings, "Setelan", glow = Color(0xFFD28CFF), size = 34.dp) }, label = { Text("Setelan") })
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onConnect, containerColor = WawDark, contentColor = WawBrightGreen) {
                GlowingIcon(Icons.Default.Add, contentDescription = "Mulai chat", tint = WawDark, glow = WawBrightGreen, size = 48.dp)
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            WorkspaceBanner(onConnect)
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Percakapan", color = WawDark, fontWeight = FontWeight.Bold, fontSize = 19.sp, modifier = Modifier.weight(1f))
                        Text("WAW Workspace", color = WawMuted, fontSize = 12.sp)
                    }
                }
                items(workspaceChats) { chat -> ChatRow(chat, onConnect) }
                item { EmptyChatHint(onConnect) }
            }
        }
    }
}

@Composable
private fun WorkspaceBanner(onConnect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(18.dp)).background(WawDark).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(WawBrightGreen), contentAlignment = Alignment.Center) {
            GlowingIcon(Icons.Default.Groups, contentDescription = "Workspace", tint = WawDark, glow = WawBrightGreen, size = 44.dp)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Workspace", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Inbox tim, label, dan assignment", color = Color(0xFFB5C8C3), fontSize = 12.sp)
        }
        Button(onClick = onConnect, colors = ButtonDefaults.buttonColors(containerColor = WawBrightGreen, contentColor = WawDark), shape = RoundedCornerShape(11.dp)) {
            Text("Buka WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChatRow(chat: ChatPreview, onConnect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { if (chat.name == "WhatsApp resmi") onConnect() }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlowingIcon(chat.icon, chat.name, tint = Color.White, glow = chat.accent, size = 52.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = WawDark)
            Text(chat.message, fontSize = 13.sp, color = WawMuted, maxLines = 1)
        }
        if (chat.time.isNotBlank()) Text(chat.time, fontSize = 11.sp, color = WawMuted)
        if (chat.unread > 0) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(20.dp).clip(CircleShape).background(WawBrightGreen), contentAlignment = Alignment.Center) {
                Text(chat.unread.toString(), fontSize = 10.sp, color = WawDark, fontWeight = FontWeight.Bold)
            }
        }
    }
    Box(Modifier.fillMaxWidth().padding(start = 81.dp).height(1.dp).background(WawLine))
}

@Composable
private fun EmptyChatHint(onConnect: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFDFF5EE)), contentAlignment = Alignment.Center) {
            GlowingIcon(Icons.Default.Link, contentDescription = "Hubungkan", tint = WawDark, glow = WawBrightGreen, size = 64.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text("Chat WhatsApp akan tampil di sini", color = WawDark, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text("WAW memakai koneksi resmi WhatsApp untuk memuat inbox bisnis. Token dan credential tetap diproses di sisi server.", color = WawMuted, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onConnect, colors = ButtonDefaults.buttonColors(containerColor = WawGreen)) { Text("Hubungkan WhatsApp resmi") }
    }
}
