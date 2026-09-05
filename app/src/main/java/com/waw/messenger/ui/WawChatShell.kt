package com.waw.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.chat.Conversation
import com.waw.messenger.chat.LiveChatRepository
import com.waw.messenger.chat.Message
import com.waw.messenger.chat.MessageStatus
import com.waw.messenger.chat.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green = Color(0xFF0AAE72)
private val TextDark = Color(0xFF17202A)
private val Muted = Color(0xFF6B7280)
private val Incoming = Color.White
private val Outgoing = Color(0xFFDFF8EC)
private val Background = Color(0xFFF3F7F5)

@Composable
fun WawChatShell(userId: String, displayName: String, baseUrl: String, token: String, onLogout: () -> Unit) {
    val repo = remember(baseUrl, token) { LiveChatRepository(baseUrl, token) }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var active by remember { mutableStateOf<Conversation?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    var showNewChat by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun refresh() = scope.launch { runCatching { conversations = repo.conversations(); users = repo.users(); error = null }.onFailure { error = it.message ?: "Gagal memuat data" }; loading = false }
    LaunchedEffect(repo) { refresh(); while (true) { delay(5000); runCatching { conversations = repo.conversations(); users = repo.users() } } }
    LaunchedEffect(active?.id) { val c = active ?: return@LaunchedEffect; while (true) { runCatching { messages = repo.messages(c.id); repo.markRead(c.id) }.onFailure { error = it.message }; delay(2000) } }
    if (active != null) ChatDetail(userId, active!!, messages, repo) { active = null; messages = emptyList(); refresh() }
    else Home(displayName, conversations.filter { search.isBlank() || it.participant.name.contains(search, true) || it.lastMessage.contains(search, true) }, search, loading, error, { search = it }, { active = it }, { showNewChat = true }, { refresh() }, onLogout)
    if (showNewChat) AlertDialog(onDismissRequest = { showNewChat = false }, title = { Text("Chat baru") }, text = { if (users.isEmpty()) Text("Tidak ada pengguna lain yang tersedia.") else Column { users.filter { it.id != userId }.forEach { u -> Row(Modifier.fillMaxWidth().clickable { scope.launch { runCatching { val id = repo.openConversation(u.id); conversations = repo.conversations(); active = conversations.firstOrNull { it.id == id } }; showNewChat = false } }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(u.name, u.online); Text(u.name, Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold) } } } }, confirmButton = { TextButton(onClick = { showNewChat = false }) { Text("Tutup") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Home(displayName: String, conversations: List<Conversation>, search: String, loading: Boolean, error: String?, onSearch: (String) -> Unit, onOpen: (Conversation) -> Unit, onNew: () -> Unit, onRefresh: () -> Unit, onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Column { Text("WAW", fontWeight = FontWeight.ExtraBold); Text(displayName, fontSize = 10.sp, color = Muted) } }, actions = { IconButton(onClick = onNew) { Icon(Icons.Default.Add, "Chat baru") }; IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Menu") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) }, containerColor = Color.White) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            Surface(Modifier.fillMaxWidth().padding(12.dp), RoundedCornerShape(14.dp), color = Background) { Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, tint = Muted); BasicTextField(search, onSearch, Modifier.weight(1f).padding(start = 8.dp), singleLine = true, decorationBox = { inner -> if (search.isBlank()) Text("Cari chat", color = Muted); inner() }) } }
            when { loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; error != null && conversations.isEmpty() -> EmptyState(error, onRefresh); conversations.isEmpty() -> EmptyState("Belum ada chat. Tekan + untuk memulai percakapan.", onNew); else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) { items(conversations, key = { it.id }) { c -> ConversationRow(c, onOpen) } } }
        }
    }
    if (showMenu) AlertDialog(onDismissRequest = { showMenu = false }, title = { Text("Pengaturan") }, text = { Text("Sesi aktif. Anda dapat keluar dari akun ini.") }, confirmButton = { TextButton(onClick = { showMenu = false; onLogout() }) { Text("Keluar") } }, dismissButton = { TextButton(onClick = { showMenu = false }) { Text("Tutup") } })
}

@Composable private fun EmptyState(text: String, action: () -> Unit) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(text, color = Muted); Spacer(Modifier.height(12.dp)); TextButton(onClick = action) { Text("Coba lagi") } } }
@Composable private fun ConversationRow(c: Conversation, onOpen: (Conversation) -> Unit) { Row(Modifier.fillMaxWidth().clickable { onOpen(c) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(c.participant.name, c.participant.online); Column(Modifier.weight(1f).padding(start = 11.dp)) { Text(c.participant.name, fontWeight = FontWeight.Bold, color = TextDark); Text(c.lastMessage.ifBlank { "Belum ada pesan" }, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }; if (c.unreadCount > 0) Surface(Modifier.size(22.dp), CircleShape, color = Green) { Box(contentAlignment = Alignment.Center) { Text(c.unreadCount.toString(), color = Color.White, fontSize = 10.sp) } } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ChatDetail(userId: String, conversation: Conversation, messages: List<Message>, repo: LiveChatRepository, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }; var sending by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope(); val list = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) list.animateScrollToItem(messages.lastIndex) }
    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(conversation.participant.name, conversation.participant.online); Column(Modifier.padding(start = 9.dp)) { Text(conversation.participant.name, fontWeight = FontWeight.Bold); Text(if (conversation.participant.online) "online" else "offline", fontSize = 10.sp, color = Muted) } } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }, actions = { IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Menu") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) }, containerColor = Background) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { items(messages, key = { it.id }) { m -> MessageBubble(m, m.senderId == userId) } }
            error?.let { Text(it, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp)) }
            Surface(color = Color.White, shadowElevation = 3.dp) { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { BasicTextField(draft, { draft = it }, Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(Background).padding(horizontal = 15.dp, vertical = 12.dp), singleLine = false, decorationBox = { inner -> if (draft.isBlank()) Text("Ketik pesan…", color = Muted); inner() }); IconButton(enabled = draft.trim().isNotEmpty() && !sending, onClick = { val text = draft.trim(); scope.launch { sending = true; error = null; runCatching { repo.sendMessage(conversation.id, text) }.onSuccess { draft = "" }.onFailure { error = it.message ?: "Pesan gagal dikirim" }; sending = false } }) { Icon(Icons.Default.Send, "Kirim", tint = if (draft.trim().isNotEmpty()) Green else Muted) } } }
        }
    }
}

@Composable private fun MessageBubble(message: Message, mine: Boolean) { Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) { Surface(color = if (mine) Outgoing else Incoming, shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth(0.84f)) { Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text(if (message.deleted) "Pesan dihapus" else message.text, color = if (message.deleted) Muted else TextDark, fontSize = 14.sp); Row(Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { Text(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)), color = Muted, fontSize = 9.sp); if (mine) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.DoneAll, null, Modifier.size(13.dp), tint = if (message.status == MessageStatus.READ) Color(0xFF1677FF) else Muted) } } } } } }
@Composable private fun Avatar(name: String, online: Boolean) { Box { Surface(Modifier.size(44.dp), CircleShape, color = Green) { Box(contentAlignment = Alignment.Center) { Text(name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold) } }; if (online) Surface(Modifier.size(11.dp).align(Alignment.BottomEnd), CircleShape, color = Green, shadowElevation = 1.dp) {} } }
