package com.waw.messenger.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
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
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waw.messenger.chat.MessageStatus
import com.waw.messenger.workspace.WorkspaceShell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WawGreen = Color(0xFF10B981)
private val WawGreenDark = Color(0xFF059669)
private val WawBlue = Color(0xFF1687F8)
private val WawText = Color(0xFF15202B)
private val WawMuted = Color(0xFF6B7280)
private val WawLine = Color(0xFFE7ECEF)
private val WawSurface = Color(0xFFF7FAF9)
private val WawIncoming = Color.White
private val WawOutgoing = Color(0xFFDDF8EC)

private enum class WawSection { CHAT, CALLS, STATUS, FEATURES, WORKSPACE }
private enum class HomeTab { ALL, UNREAD, WORKSPACE }

private data class DemoContact(val id: String, val name: String, val status: String, val unread: Int, val preview: String, val online: Boolean)
private data class UiMessage(val id: String, val text: String, val mine: Boolean, val time: String, val status: MessageStatus)

@Composable
fun WawChatShell(modifier: Modifier = Modifier) {
    var section by remember { mutableStateOf(WawSection.CHAT) }
    var homeTab by remember { mutableStateOf(HomeTab.ALL) }
    var activeChat by remember { mutableStateOf<String?>(null) }
    val contacts = remember {
        listOf(
            DemoContact("andi", "Andi", "online", 2, "Sedang mengetik…", true),
            DemoContact("budi", "Budi", "terakhir dilihat 5 mnt lalu", 0, "File sudah saya kirim", false),
            DemoContact("citra", "Citra", "online", 1, "Oke, nanti saya cek", true),
            DemoContact("waw", "WAW Assistant", "online • WAW AI", 0, "Workspace siap digunakan", true)
        )
    }

    Crossfade(targetState = activeChat != null, animationSpec = tween(220), label = "chat-open") { chatOpen ->
        if (chatOpen) {
            ChatScreen(
                contact = contacts.firstOrNull { it.id == activeChat } ?: contacts.first(),
                onBack = { activeChat = null }
            )
        } else if (section == WawSection.WORKSPACE) {
            WorkspaceShell(modifier)
        } else {
            WawHome(
                section = section,
                homeTab = homeTab,
                onHomeTab = { homeTab = it },
                onSection = { section = it },
                onOpenChat = { activeChat = it },
                contacts = contacts,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WawHome(
    section: WawSection,
    homeTab: HomeTab,
    onHomeTab: (HomeTab) -> Unit,
    onSection: (WawSection) -> Unit,
    onOpenChat: (String) -> Unit,
    contacts: List<DemoContact>,
    modifier: Modifier = Modifier
) {
    val topTabs = listOf("Chat", "Panggilan", "Status", "Fitur", "Workspace")
    val topIndex = WawSection.values().indexOf(section)
    val topState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(topIndex) {
        topState.animateScrollToItem(topIndex.coerceIn(0, topTabs.lastIndex))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WawLogo(38.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("WAW", fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = WawText)
                                Text("Work • Assist • WhatsApp companion", fontSize = 10.sp, color = WawMuted)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(topTabs.size) { index ->
                        val selected = index == topIndex
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .clickable {
                                    val target = WawSection.values()[index]
                                    onSection(target)
                                    coroutine.launch { topState.animateScrollToItem(index) }
                                },
                            color = if (selected) WawGreen else Color.Transparent,
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text(
                                topTabs[index],
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                color = if (selected) Color.White else WawMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                BottomItem("Chat", Icons.Default.Home, WawSection.CHAT, section, onSection)
                BottomItem("Panggilan", Icons.Default.Call, WawSection.CALLS, section, onSection)
                BottomItem("Status", Icons.Default.Info, WawSection.STATUS, section, onSection)
                BottomItem("Fitur", Icons.Default.TaskAlt, WawSection.FEATURES, section, onSection)
                BottomItem("Workspace", Icons.Default.Folder, WawSection.WORKSPACE, section, onSection)
            }
        }
    ) { padding ->
        when (section) {
            WawSection.CHAT -> ChatHomeBody(homeTab, onHomeTab, contacts, onOpenChat, Modifier.padding(padding))
            WawSection.CALLS -> CallsBody(Modifier.padding(padding))
            WawSection.STATUS -> StatusBody(Modifier.padding(padding))
            WawSection.FEATURES -> FeaturesBody(Modifier.padding(padding))
            WawSection.WORKSPACE -> Unit
        }
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: WawSection, selected: WawSection, onSelect: (WawSection) -> Unit) {
    NavigationBarItem(
        selected = selected == value,
        onClick = { onSelect(value) },
        icon = { Icon(icon, null) },
        label = { Text(label, fontSize = 10.sp) }
    )
}

@Composable
private fun ChatHomeBody(
    homeTab: HomeTab,
    onHomeTab: (HomeTab) -> Unit,
    contacts: List<DemoContact>,
    onOpenChat: (String) -> Unit,
    modifier: Modifier
) {
    val listState = rememberLazyListState()
    val visible = when (homeTab) {
        HomeTab.ALL -> contacts
        HomeTab.UNREAD -> contacts.filter { it.unread > 0 }
        HomeTab.WORKSPACE -> contacts.filter { it.id == "waw" }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                HomeChip("Semua", homeTab == HomeTab.ALL) { onHomeTab(HomeTab.ALL) }
                HomeChip("Belum dibaca", homeTab == HomeTab.UNREAD) { onHomeTab(HomeTab.UNREAD) }
                HomeChip("Workspace", homeTab == HomeTab.WORKSPACE) { onHomeTab(HomeTab.WORKSPACE) }
            }
        }
        item { WorkspaceQuickAccess() }
        item { WawInsight() }
        item {
            Text("Pesan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WawText, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
        }
        items(visible, key = { it.id }) { contact ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 5 },
                exit = fadeOut(tween(120))
            ) {
                ConversationRow(contact, onOpenChat)
            }
        }
    }
}

@Composable
private fun HomeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) WawGreen else WawSurface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = if (selected) Color.White else WawText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WorkspaceQuickAccess() {
    Card(colors = CardDefaults.cardColors(containerColor = WawSurface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Workspace", fontWeight = FontWeight.Bold, color = WawText, modifier = Modifier.weight(1f))
                Text("Quick Access", color = WawGreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                WorkspaceTile("Dokumen", Icons.Default.FileCopy, WawBlue)
                WorkspaceTile("Tugas", Icons.Default.TaskAlt, WawGreen)
                WorkspaceTile("Kalender", Icons.Default.Info, Color(0xFF8B5CF6))
                WorkspaceTile("File", Icons.Default.Folder, Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
private fun WorkspaceTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Surface(color = Color.White, shape = RoundedCornerShape(15.dp), modifier = Modifier.size(52.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(25.dp)) }
        }
        Text(label, fontSize = 10.sp, color = WawMuted, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun WawInsight() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, WawLine), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            WawLogo(42.dp)
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("WAW Insight", fontWeight = FontWeight.Bold, color = WawText)
                Text("Semua aktivitas Workspace tetap terpisah dan dikendalikan WAW.", fontSize = 11.sp, color = WawMuted)
            }
            Icon(Icons.Default.Wifi, null, tint = WawGreen)
        }
    }
}

@Composable
private fun ConversationRow(contact: DemoContact, onOpenChat: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).clickable { onOpenChat(contact.id) }.padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(contact.name, contact.online)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(contact.name, color = WawText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.preview.contains("mengetik")) TypingDots()
                Text(contact.preview, color = if (contact.preview.contains("mengetik")) WawGreenDark else WawMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("09:4${contact.name.length}", fontSize = 10.sp, color = WawMuted)
            if (contact.unread > 0) {
                Spacer(Modifier.height(5.dp))
                Surface(color = WawGreen, shape = CircleShape) { Text(contact.unread.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) }
            }
        }
    }
}

@Composable
private fun Avatar(name: String, online: Boolean) {
    Box(Modifier.size(52.dp)) {
        if (name == "WAW Assistant") WawLogo(52.dp) else {
            Surface(color = if (name.length % 2 == 0) Color(0xFFE5F7F0) else Color(0xFFEAF3FF), shape = CircleShape, modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) { Text(name.take(1), color = if (name.length % 2 == 0) WawGreenDark else WawBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
            }
        }
        if (online) Box(Modifier.size(12.dp).align(Alignment.BottomEnd).background(WawGreen, CircleShape).border(2.dp, Color.White, CircleShape))
    }
}

@Composable
private fun WawLogo(size: androidx.compose.ui.unit.Dp) {
    // Temporary vector treatment matching the supplied WAW green/blue identity.
    Box(Modifier.size(size).clip(CircleShape).background(Color(0xFFE9FFF7)), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Surface(color = WawGreen, shape = CircleShape, modifier = Modifier.size(size * .58f)) {}
            Surface(color = WawBlue, shape = CircleShape, modifier = Modifier.size(size * .58f).padding(start = 1.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("W", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * .28f).sp) }
            }
        }
    }
}

@Composable
private fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "typing-phase")
    Row(Modifier.width(18.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            Box(Modifier.size(4.dp).scale(0.7f + phase * (0.3f + index * .06f)).background(WawGreen, CircleShape))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(contact: DemoContact, onBack: () -> Unit) {
    val messages = remember(contact.id) {
        mutableStateListOf(
            UiMessage("1", "Halo, selamat datang di WAW 👋", false, "09:41", MessageStatus.READ),
            UiMessage("2", "Tampilannya sudah putih dan simpel.", true, "09:42", MessageStatus.READ),
            UiMessage("3", "Iya. Workspace juga tetap ada di dalam WAW.", false, "09:43", MessageStatus.READ)
        )
    }
    var draft by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(contact.id == "andi") }
    var recording by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size, typing) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LaunchedEffect(contact.id) {
        delay(1600)
        typing = false
    }

    Scaffold(
        containerColor = WawSurface,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(contact.name, contact.online)
                        Column(Modifier.padding(start = 9.dp)) {
                            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(if (typing) "sedang mengetik…" else if (contact.online) "online" else contact.status, fontSize = 11.sp, color = if (typing) WawGreenDark else WawMuted)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali", tint = WawText) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.VideoCall, "Video", tint = WawText) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Call, "Panggilan", tint = WawText) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Composer(
                text = draft,
                onTextChange = { draft = it; typing = it.isNotBlank() },
                recording = recording,
                sending = sending,
                onRecord = { recording = !recording },
                onSend = {
                    val clean = draft.trim()
                    if (clean.isNotEmpty()) {
                        sending = true
                        messages.add(UiMessage("m-${messages.size}", clean, true, "09:4${messages.size}", MessageStatus.SENDING))
                        draft = ""
                        scope.launch {
                            delay(260)
                            val index = messages.lastIndex
                            messages[index] = messages[index].copy(status = MessageStatus.SENT)
                            delay(320)
                            messages[index] = messages[index].copy(status = MessageStatus.DELIVERED)
                            delay(520)
                            messages[index] = messages[index].copy(status = MessageStatus.READ)
                            sending = false
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            item { Text("Hari ini", modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), color = WawMuted, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            items(messages, key = { it.id }) { message ->
                AnimatedMessage(message)
            }
            item {
                AnimatedVisibility(typing, enter = fadeIn() + expandVertically(), exit = fadeOut()) {
                    TypingBubble()
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AnimatedMessage(message: UiMessage) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + slideInHorizontally(tween(230)) { if (message.mine) it / 3 else -it / 3 } + scaleIn(tween(180), initialScale = .96f),
        exit = fadeOut(tween(120)) + slideOutHorizontally(tween(120)) { if (message.mine) it / 4 else -it / 4 }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start) {
            Surface(
                color = if (message.mine) WawOutgoing else WawIncoming,
                shape = if (message.mine) RoundedCornerShape(18.dp, 6.dp, 18.dp, 18.dp) else RoundedCornerShape(6.dp, 18.dp, 18.dp, 18.dp),
                shadowElevation = 1.dp
            ) {
                Row(Modifier.padding(start = 12.dp, end = 9.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.Bottom) {
                    Text(message.text, color = WawText, fontSize = 14.sp, modifier = Modifier.padding(end = 9.dp))
                    Text(message.time, color = WawMuted, fontSize = 9.sp)
                    if (message.mine) {
                        Spacer(Modifier.width(3.dp))
                        MessageStatusIcon(message.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> Icon(Icons.Default.Check, null, tint = WawMuted, modifier = Modifier.size(12.dp))
        MessageStatus.SENT -> Icon(Icons.Default.Check, null, tint = WawMuted, modifier = Modifier.size(12.dp))
        MessageStatus.DELIVERED, MessageStatus.READ -> Icon(Icons.Default.DoneAll, null, tint = if (status == MessageStatus.READ) WawBlue else WawMuted, modifier = Modifier.size(13.dp))
        MessageStatus.FAILED -> Icon(Icons.Default.Info, null, tint = Color.Red, modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun TypingBubble() {
    Surface(color = Color.White, shape = RoundedCornerShape(7.dp, 18.dp, 18.dp, 18.dp), shadowElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            TypingDots()
            Text(" sedang mengetik…", fontSize = 11.sp, color = WawMuted)
        }
    }
}

@Composable
private fun Composer(
    text: String,
    onTextChange: (String) -> Unit,
    recording: Boolean,
    sending: Boolean,
    onRecord: () -> Unit,
    onSend: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 8.dp).padding(WindowInsets.navigationBars.asPaddingValues())) {
            AnimatedVisibility(recording, enter = fadeIn() + expandVertically(), exit = fadeOut()) {
                VoiceWaveform(onStop = onRecord)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.animateContentSize(animationSpec = spring())) {
                IconButton(onClick = {}) { Icon(Icons.Default.Add, "Tambah", tint = WawGreenDark) }
                Surface(color = WawSurface, shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 11.dp),
                            textStyle = TextStyle(fontSize = 14.sp, color = WawText),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (text.isEmpty()) Text("Tulis pesan…", color = WawMuted, fontSize = 14.sp)
                                inner()
                            }
                        )
                        IconButton(onClick = {}) { Icon(Icons.Default.EmojiEmotions, "Emoji", tint = WawMuted) }
                    }
                }
                Spacer(Modifier.width(4.dp))
                val pulse by animateFloatAsState(if (sending) .88f else 1f, tween(180), label = "send-pulse")
                Surface(color = WawGreen, shape = CircleShape, modifier = Modifier.size(47.dp).scale(pulse).clickable { if (text.isNotBlank()) onSend() else onRecord() }) {
                    Box(contentAlignment = Alignment.Center) {
                        if (text.isBlank()) Icon(Icons.Default.KeyboardVoice, "Rekam suara", tint = Color.White) else Icon(Icons.Default.Send, "Kirim", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceWaveform(onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "voice")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "voice-phase")
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onStop) { Icon(Icons.Default.KeyboardVoice, "Selesai", tint = WawGreenDark) }
        Canvas(Modifier.weight(1f).height(38.dp)) {
            val bars = 34
            repeat(bars) { i ->
                val x = size.width * i / bars
                val h = size.height * (.18f + .7f * kotlin.math.abs(kotlin.math.sin(i * .75 + phase * 3.2)))
                drawLine(WawGreen, Offset(x, size.height / 2 - h / 2), Offset(x, size.height / 2 + h / 2), 3.dp.toPx(), StrokeCap.Round)
            }
        }
        Text("0:04", fontSize = 11.sp, color = WawMuted, modifier = Modifier.padding(horizontal = 6.dp))
    }
}

@Composable
private fun CallsBody(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageTitle("Panggilan", "Riwayat panggilan WAW") }
        item { CallRow("Andi", "Panggilan video • 09:31", true) }
        item { CallRow("Budi", "Panggilan suara • 08:52", false) }
        item { CallRow("Citra", "Panggilan terjawab • Kemarin", true) }
    }
}

@Composable
private fun CallRow(name: String, detail: String, video: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(name, false)
        Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(name, fontWeight = FontWeight.Bold); Text(detail, color = WawMuted, fontSize = 11.sp) }
        Icon(if (video) Icons.Default.VideoCall else Icons.Default.Phone, null, tint = WawGreenDark)
    }
}

@Composable
private fun StatusBody(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageTitle("Status", "Update terbaru dari kontak") }
        item { StatusCard("WAW", "Workspace aktif • 2 menit lalu") }
        item { StatusCard("Andi", "Sedang bekerja • 12 menit lalu") }
        item { StatusCard("Citra", "Selesai meeting • 1 jam lalu") }
    }
}

@Composable
private fun StatusCard(name: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = WawSurface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, true)
            Column(Modifier.padding(start = 12.dp)) { Text(name, fontWeight = FontWeight.Bold); Text(detail, color = WawMuted, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun FeaturesBody(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { PageTitle("Fitur", "Tools WAW yang siap dipakai") }
        item { FeatureCard("WAW Shield", "Anti-phishing & blocklist berisiko", Icons.Default.Info) }
        item { FeatureCard("Network Diagnostics", "IP lokal, koneksi dan status jaringan", Icons.Default.Wifi) }
        item { FeatureCard("Secure Vault", "Kunci Workspace dengan biometrik", Icons.Default.Settings) }
        item { FeatureCard("File & PDF", "Kelola dokumen tanpa meninggalkan WAW", Icons.Default.FileCopy) }
        item { FeatureCard("Scanner", "Scan dokumen ke PDF", Icons.Default.CameraAlt) }
    }
}

@Composable
private fun FeatureCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(WawSurface).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = WawGreenDark) } }
        Column(Modifier.padding(start = 12.dp)) { Text(title, fontWeight = FontWeight.Bold, color = WawText); Text(detail, color = WawMuted, fontSize = 11.sp) }
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = WawText)
        Text(subtitle, color = WawMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
    }
}
