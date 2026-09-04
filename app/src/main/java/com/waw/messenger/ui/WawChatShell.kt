package com.waw.messenger.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.chat.MessageStatus
import com.waw.messenger.workspace.WorkspaceShell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

private val WawGreen = Color(0xFF10B981)
private val WawGreenDark = Color(0xFF059669)
private val WawBlue = Color(0xFF1687F8)
private val WawText = Color(0xFF15202B)
private val WawMuted = Color(0xFF6B7280)
private val WawLine = Color(0xFFE7ECEF)
private val WawSurface = Color(0xFFF7FAF9)
private val WawOutgoing = Color(0xFFDDF8EC)

private enum class WawSection { CHAT, CALLS, STATUS, FEATURES, WORKSPACE }
private enum class HomeTab { ALL, UNREAD, WORKSPACE }

private data class Contact(
    val id: String,
    val name: String,
    val status: String,
    val preview: String,
    val unread: Int,
    val online: Boolean
)

private data class UiMessage(
    val id: String,
    val text: String,
    val mine: Boolean,
    val time: String,
    val status: MessageStatus
)

@Composable
fun WawChatShell(modifier: Modifier = Modifier) {
    var section by remember { mutableStateOf(WawSection.CHAT) }
    var homeTab by remember { mutableStateOf(HomeTab.ALL) }
    var activeChat by remember { mutableStateOf<String?>(null) }
    val contacts = remember {
        listOf(
            Contact("tim", "Tim WAW", "online • sedang mengetik...", "Sedang mengetik...", 3, true),
            Contact("marketing", "Tim Marketing – Q4 Campaign", "online", "Rina: Deck final sudah di-up...", 3, true),
            Contact("client", "Client • PT Nusantara", "online", "Oke, nanti saya cek", 1, true),
            Contact("design", "Design System Team", "kemarin", "You: Approved ✅ Figma link di wor...", 0, false),
            Contact("dev", "Dev Team", "kemarin", "Linting passed. Deploying...", 0, false),
            Contact("sari", "Mba Sari", "Sen", "Siap, invoice sudah dikirim ✅", 0, false)
        )
    }

    Crossfade(targetState = activeChat != null, animationSpec = tween(220), label = "open-chat") { open ->
        if (open) {
            ChatScreen(
                contact = contacts.firstOrNull { it.id == activeChat } ?: contacts.first(),
                onBack = { activeChat = null }
            )
        } else if (section == WawSection.WORKSPACE) {
            WorkspaceShell(modifier)
        } else {
            HomeScreen(
                section = section,
                homeTab = homeTab,
                contacts = contacts,
                onHomeTab = { homeTab = it },
                onSection = { section = it },
                onOpenChat = { activeChat = it },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    section: WawSection,
    homeTab: HomeTab,
    contacts: List<Contact>,
    onHomeTab: (HomeTab) -> Unit,
    onSection: (WawSection) -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier
) {
    val tabs = listOf("Chat", "Panggilan", "Status", "Fitur", "Workspace")
    val topState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val selectedIndex = section.ordinal

    LaunchedEffect(selectedIndex) {
        topState.animateScrollToItem(selectedIndex)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WawLogo(40.dp)
                            Spacer(Modifier.width(9.dp))
                            Column {
                                Text("WAW", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = WawText)
                                Text("WhatsApp Workspace", fontSize = 10.sp, color = WawMuted)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Search, "Cari", tint = WawText) }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Menu", tint = WawText) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                LazyRow(
                    state = topState,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tabs.size) { index ->
                        val selected = index == selectedIndex
                        Surface(
                            color = if (selected) WawText else WawSurface,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable {
                                onSection(WawSection.entries[index])
                                scope.launch { topState.animateScrollToItem(index) }
                            }
                        ) {
                            Text(
                                tabs[index],
                                modifier = Modifier.padding(horizontal = 17.dp, vertical = 9.dp),
                                color = if (selected) Color.White else WawMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        },
        bottomBar = {
            WawBottomBar(section, onSection)
        }
    ) { padding ->
        when (section) {
            WawSection.CHAT -> ChatHome(homeTab, contacts, onHomeTab, onOpenChat, Modifier.padding(padding))
            WawSection.CALLS -> CallsBody(Modifier.padding(padding))
            WawSection.STATUS -> StatusBody(Modifier.padding(padding))
            WawSection.FEATURES -> FeaturesBody(Modifier.padding(padding))
            WawSection.WORKSPACE -> Unit
        }
    }
}

@Composable
private fun WawBottomBar(section: WawSection, onSection: (WawSection) -> Unit) {
    Surface(color = Color.White, shadowElevation = 3.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceAround) {
            BottomItem("Chat", Icons.Default.Home, WawSection.CHAT, section, onSection)
            BottomItem("Panggilan", Icons.Default.Call, WawSection.CALLS, section, onSection)
            BottomItem("Status", Icons.Default.Info, WawSection.STATUS, section, onSection)
            BottomItem("Fitur", Icons.Default.TaskAlt, WawSection.FEATURES, section, onSection)
            BottomItem("Workspace", Icons.Default.Folder, WawSection.WORKSPACE, section, onSection)
        }
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: WawSection, selected: WawSection, onSelect: (WawSection) -> Unit) {
    val scale by animateFloatAsState(if (selected == value) 1.08f else 1f, tween(180), label = "nav-scale")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp).clickable { onSelect(value) }.padding(vertical = 3.dp).scale(scale)
    ) {
        Icon(icon, null, tint = if (selected == value) WawGreenDark else WawMuted, modifier = Modifier.size(21.dp))
        Text(label, fontSize = 9.sp, color = if (selected == value) WawText else WawMuted, fontWeight = if (selected == value) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ChatHome(homeTab: HomeTab, contacts: List<Contact>, onHomeTab: (HomeTab) -> Unit, onOpenChat: (String) -> Unit, modifier: Modifier) {
    val visible = when (homeTab) {
        HomeTab.ALL -> contacts
        HomeTab.UNREAD -> contacts.filter { it.unread > 0 }
        HomeTab.WORKSPACE -> contacts.filter { it.id == "tim" }
    }
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(top = 9.dp, bottom = 7.dp)) {
                item { HomeChip("Semua", homeTab == HomeTab.ALL) { onHomeTab(HomeTab.ALL) } }
                item { HomeChip("Belum dibaca", homeTab == HomeTab.UNREAD) { onHomeTab(HomeTab.UNREAD) } }
                item { HomeChip("Workspace", homeTab == HomeTab.WORKSPACE) { onHomeTab(HomeTab.WORKSPACE) } }
            }
        }
        item { WorkspaceQuickAccess() }
        item { WawInsight() }
        item { Text("OBROLAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WawMuted, modifier = Modifier.padding(top = 13.dp, bottom = 3.dp)) }
        items(visible, key = { it.id }) { contact ->
            AnimatedVisibility(true, enter = fadeIn(tween(180)) + slideInVertically(tween(240)) { it / 5 }, exit = fadeOut(tween(120))) {
                ConversationRow(contact, onOpenChat)
            }
        }
    }
}

@Composable
private fun HomeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) WawText else WawSurface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) { Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = if (selected) Color.White else WawText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun WorkspaceQuickAccess() {
    Surface(color = WawSurface, shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Workspace Quick Access", color = WawText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Lihat semua", color = WawGreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickTile("Dokumen", Icons.Default.FileCopy, WawBlue, "12 file")
                QuickTile("Tugas", Icons.Default.TaskAlt, WawGreen, "8 aktif")
                QuickTile("Kalender", Icons.Default.Info, Color(0xFF8B5CF6), "3 meeting")
                QuickTile("File", Icons.Default.Folder, Color(0xFFF59E0B), "2.4 GB")
            }
        }
    }
}

@Composable
private fun QuickTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, detail: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Surface(color = Color.White, shape = RoundedCornerShape(13.dp), modifier = Modifier.size(50.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp)) }
        }
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WawText, modifier = Modifier.padding(top = 4.dp))
        Text(detail, fontSize = 8.sp, color = WawMuted)
    }
}

@Composable
private fun WawInsight() {
    Surface(color = Color.White, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth().padding(top = 7.dp).border(1.dp, WawLine, RoundedCornerShape(17.dp))) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            WawLogo(38.dp)
            Column(Modifier.weight(1f).padding(start = 9.dp)) {
                Text("WAW Insight", fontWeight = FontWeight.Bold, color = WawText)
                Text("Workspace tetap terpisah dan dikendalikan WAW.", fontSize = 10.sp, color = WawMuted)
            }
            Text("●", color = WawGreen, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ConversationRow(contact: Contact, onOpenChat: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onOpenChat(contact.id) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(contact.name, contact.online)
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text(contact.name, color = WawText, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.preview.contains("mengetik")) TypingDots()
                Text(contact.preview, color = if (contact.preview.contains("mengetik")) WawGreenDark else WawMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(if (contact.unread > 0) "10:42" else "Kemarin", fontSize = 9.sp, color = WawMuted)
            if (contact.unread > 0) Surface(color = WawGreen, shape = CircleShape, modifier = Modifier.padding(top = 4.dp).size(21.dp)) { Box(contentAlignment = Alignment.Center) { Text(contact.unread.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
        }
    }
}

@Composable
private fun Avatar(name: String, online: Boolean) {
    Box {
        Surface(color = WawGreenDark, shape = CircleShape, modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Text(name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
        if (online) Surface(color = WawGreen, shape = CircleShape, modifier = Modifier.size(12.dp).align(Alignment.BottomEnd).border(2.dp, Color.White, CircleShape)) {}
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    val pulse by transition.animateFloat(0.45f, 1f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "typing-pulse")
    Row(Modifier.padding(end = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index -> Text("•", color = WawGreen, fontSize = 14.sp, modifier = Modifier.alpha(if (index == 1) pulse else 0.7f)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(contact: Contact, onBack: () -> Unit) {
    val messages = remember {
        mutableStateListOf(
            UiMessage("1", "Hai! Logo baru WAW udah aku finalize pakai aset 3D hijau-teal ✨\nMirip vibe Meta AI kan? Clean glass + animasi masuk.", false, "10:42", MessageStatus.READ),
            UiMessage("2", "Gokil! Jauh lebih premium 🔥 pakai ini buat header + app icon. Jangan pakai WA logo lagi ya.", true, "10:43", MessageStatus.READ),
            UiMessage("3", "Aku tambahin animasi: pesan masuk slide dari kiri, terkirim slide kanan + double check biru pop!", false, "10:44", MessageStatus.READ),
            UiMessage("4", "Perfect. Auto-loop tiap 5 detik biar demo keren terus ✨", true, "10:44", MessageStatus.READ)
        )
    }
    var draft by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(true) }
    var recording by remember { mutableStateOf(false) }
    var attachmentProgress by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size, typing) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LaunchedEffect(typing) {
        if (typing) {
            delay(5000)
            typing = false
        }
    }
    LaunchedEffect(attachmentProgress) {
        if (attachmentProgress > 0f && attachmentProgress < 1f) {
            delay(90)
            attachmentProgress = (attachmentProgress + 0.035f).coerceAtMost(1f)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5FAF8),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(contact.name, contact.online)
                        Column(Modifier.padding(start = 9.dp)) {
                            Text(contact.name, color = WawText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(if (typing) "online • sedang mengetik..." else contact.status, color = WawGreenDark, fontSize = 10.sp)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali", tint = WawText) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.VideoCall, "Video", tint = WawText) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Phone, "Telepon", tint = WawText) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Menu", tint = WawText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                item { DayPill("Hari ini") }
                items(messages, key = { it.id }) { message -> MessageBubble(message) }
                if (attachmentProgress > 0f && attachmentProgress < 1f) item { AttachmentProgress(attachmentProgress) }
                if (typing) item { TypingBubble() }
            }
            if (recording) VoiceWaveform { recording = false }
            Composer(
                text = draft,
                onText = { draft = it },
                recording = recording,
                onRecord = { recording = !recording },
                onAttach = { attachmentProgress = 0.05f },
                onSend = {
                    val value = draft.trim()
                    if (value.isNotEmpty()) {
                        val id = "local-${System.currentTimeMillis()}"
                        messages.add(UiMessage(id, value, true, "10:45", MessageStatus.SENDING))
                        draft = ""
                        typing = false
                        scope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                            delay(350)
                            val i = messages.indexOfFirst { it.id == id }
                            if (i >= 0) messages[i] = messages[i].copy(status = MessageStatus.SENT)
                            delay(450)
                            if (i >= 0) messages[i] = messages[i].copy(status = MessageStatus.DELIVERED)
                            delay(650)
                            if (i >= 0) messages[i] = messages[i].copy(status = MessageStatus.READ)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DayPill(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Surface(color = Color.White, shape = RoundedCornerShape(15.dp)) { Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp), color = WawMuted, fontSize = 10.sp) } }
}

@Composable
private fun MessageBubble(message: UiMessage) {
    val enter = fadeIn(tween(180)) + if (message.mine) slideInHorizontally(tween(260)) { it / 2 } else slideInHorizontally(tween(260)) { -it / 2 }
    AnimatedVisibility(true, enter = enter + scaleIn(tween(180), initialScale = 0.96f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start) {
            Surface(
                color = if (message.mine) WawOutgoing else Color.White,
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier.fillMaxWidth(0.82f).border(1.dp, if (message.mine) Color.Transparent else WawLine, RoundedCornerShape(17.dp))
            ) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
                    Text(message.text, color = WawText, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Text(message.time, fontSize = 9.sp, color = WawMuted)
                        if (message.mine) {
                            Spacer(Modifier.width(4.dp))
                            MessageStatusIcon(message.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    AnimatedContent(status, label = "message-status") { current ->
        when (current) {
            MessageStatus.SENDING -> Text("◷", color = WawMuted, fontSize = 12.sp)
            MessageStatus.SENT -> Icon(Icons.Default.Check, null, tint = WawMuted, modifier = Modifier.size(13.dp))
            MessageStatus.DELIVERED -> Icon(Icons.Default.DoneAll, null, tint = WawBlue, modifier = Modifier.size(14.dp))
            MessageStatus.READ -> Icon(Icons.Default.DoneAll, null, tint = WawBlue, modifier = Modifier.size(15.dp))
            MessageStatus.FAILED -> Text("!", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TypingBubble() {
    Surface(color = Color.White, shape = RoundedCornerShape(17.dp), modifier = Modifier.padding(start = 28.dp)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Tim WAW sedang mengetik", color = WawMuted, fontSize = 11.sp)
            Spacer(Modifier.width(6.dp))
            TypingDots()
        }
    }
}

@Composable
private fun AttachmentProgress(progress: Float) {
    Surface(color = Color.White, shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().border(1.dp, WawLine, RoundedCornerShape(15.dp))) {
        Column(Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileCopy, null, tint = WawBlue)
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("preview_design.mp4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WawText)
                    Text("12.4 MB • MP4", fontSize = 9.sp, color = WawMuted)
                }
                Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = WawMuted)
            }
            Spacer(Modifier.height(7.dp))
            Surface(color = WawLine, shape = RoundedCornerShape(5.dp), modifier = Modifier.fillMaxWidth().height(5.dp)) {
                Surface(color = WawGreen, shape = RoundedCornerShape(5.dp), modifier = Modifier.fillMaxWidth(progress).height(5.dp)) {}
            }
        }
    }
}

@Composable
private fun Composer(text: String, onText: (String) -> Unit, recording: Boolean, onRecord: () -> Unit, onAttach: () -> Unit, onSend: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAttach) { Icon(Icons.Default.Add, "Lampiran", tint = WawText) }
            Surface(color = WawSurface, shape = RoundedCornerShape(23.dp), modifier = Modifier.weight(1f)) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(value = text, onValueChange = onText, modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodyMedium.copy(color = WawText), singleLine = true, decorationBox = { inner ->
                        Box { if (text.isEmpty()) Text("Ketik pesan...", color = WawMuted, fontSize = 13.sp); inner() }
                    })
                    Icon(Icons.Default.EmojiEmotions, null, tint = WawMuted, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
            Surface(color = if (text.isNotBlank()) WawGreen else WawGreenDark, shape = CircleShape, modifier = Modifier.size(45.dp).clickable { if (text.isNotBlank()) onSend() else onRecord() }) {
                Box(contentAlignment = Alignment.Center) { Icon(if (text.isNotBlank()) Icons.Default.Send else Icons.Default.KeyboardVoice, null, tint = Color.White) }
            }
        }
    }
}

@Composable
private fun VoiceWaveform(onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "voice")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "voice-phase")
    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onStop) { Icon(Icons.Default.KeyboardVoice, "Selesai", tint = WawGreenDark) }
            Canvas(Modifier.weight(1f).height(36.dp)) {
                repeat(34) { i ->
                    val x = size.width * i / 34f
                    val h = size.height * (0.18f + 0.7f * abs(sin(i * 0.75f + phase * 3.2f)))
                    drawLine(WawGreen, Offset(x, size.height / 2f - h / 2f), Offset(x, size.height / 2f + h / 2f), 3.dp.toPx(), StrokeCap.Round)
                }
            }
            Text("0:04", fontSize = 10.sp, color = WawMuted, modifier = Modifier.padding(horizontal = 7.dp))
        }
    }
}

@Composable
private fun CallsBody(modifier: Modifier) {
    SimpleSection(modifier, "Panggilan", "Riwayat panggilan WAW akan tampil di sini.", Icons.Default.Call)
}

@Composable
private fun StatusBody(modifier: Modifier) {
    SimpleSection(modifier, "Status", "Status, update, dan aktivitas tim WAW.", Icons.Default.Info)
}

@Composable
private fun FeaturesBody(modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Color.White).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Fitur WAW", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = WawText)
        FeatureCard("WAW Shield", "Anti-phishing dan perlindungan link berisiko", Icons.Default.Settings)
        FeatureCard("Workspace", "Dokumen, tugas, kalender, dan file", Icons.Default.Folder)
        FeatureCard("Keamanan", "Biometric Gate dan Secure Vault", Icons.Default.TaskAlt)
    }
}

@Composable
private fun FeatureCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(color = WawSurface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.White, shape = CircleShape, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = WawGreenDark) } }
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = WawText)
                Text(detail, fontSize = 11.sp, color = WawMuted)
            }
        }
    }
}

@Composable
private fun SimpleSection(modifier: Modifier, title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier.fillMaxSize().background(Color.White).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = WawSurface, shape = CircleShape, modifier = Modifier.size(70.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = WawGreenDark, modifier = Modifier.size(34.dp)) } }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = WawText, modifier = Modifier.padding(top = 14.dp))
        Text(detail, color = WawMuted, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun WawLogo(size: androidx.compose.ui.unit.Dp) {
    Surface(color = WawGreenDark, shape = CircleShape, modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Surface(color = WawBlue, shape = CircleShape, modifier = Modifier.size(size * 0.72f)) {
                Box(contentAlignment = Alignment.Center) { Text("W", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * 0.32f).sp) }
            }
        }
    }
}
