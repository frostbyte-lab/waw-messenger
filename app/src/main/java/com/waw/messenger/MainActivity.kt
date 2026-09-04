package com.waw.messenger

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waw.messenger.chat.ChatRepository
import com.waw.messenger.chat.Conversation
import com.waw.messenger.chat.LocalChatRepository
import com.waw.messenger.chat.Message
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WawApp() }
    }
}

@Composable
fun WawApp() {
    val repository: ChatRepository = remember { LocalChatRepository("me") }
    var selected by remember { mutableStateOf<Conversation?>(null) }
    MaterialTheme {
        if (selected == null) ConversationList(repository) { selected = it }
        else ChatScreen(repository, selected!!, onBack = { selected = null })
    }
}

@Composable
private fun ConversationList(repository: ChatRepository, onOpen: (Conversation) -> Unit) {
    val conversations by repository.conversations().collectAsState(emptyList())
    Scaffold(topBar = { TopAppBar(title = { Text("WAW") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(conversations, key = { it.id }) { conversation ->
                Row(Modifier.fillMaxWidth().clickable { onOpen(conversation) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(conversation.participant.name, style = MaterialTheme.typography.titleMedium)
                        Text(conversation.lastMessage.ifBlank { "Mulai percakapan" })
                    }
                    if (conversation.unreadCount > 0) Text("${conversation.unreadCount}")
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ChatScreen(repository: ChatRepository, conversation: Conversation, onBack: () -> Unit) {
    val messages by repository.messages(conversation.id).collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    LaunchedEffect(conversation.id) { repository.markRead(conversation.id) }

    Scaffold(topBar = {
        TopAppBar(title = { Text(conversation.participant.name) }, navigationIcon = {
            Button(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) { Text("‹") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), reverseLayout = true) {
                items(messages.asReversed(), key = { it.id }) { MessageBubble(it, it.senderId == "me") }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), placeholder = { Text("Tulis pesan…") })
                Spacer(Modifier.padding(4.dp))
                Button(enabled = text.isNotBlank(), onClick = {
                    val value = text.trim(); text = ""
                    scope.launch { repository.sendMessage(conversation.id, "me", value) }
                }) { Text("Kirim") }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, mine: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Text(
            text = if (message.deleted) "Pesan dihapus" else message.text,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
