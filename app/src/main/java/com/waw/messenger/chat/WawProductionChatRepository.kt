package com.waw.messenger.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class WawApiClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String?,
    private val client: OkHttpClient = OkHttpClient()
) {
    private fun request(path: String): JSONObject {
        val token = tokenProvider() ?: error("AUTH_REQUIRED")
        val response = client.newCall(
            Request.Builder()
                .url(baseUrl.trimEnd('/') + path)
                .header("Authorization", "Bearer $token")
                .get()
                .build()
        ).execute()
        val raw = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("HTTP_${response.code}:$raw")
        return JSONObject(raw)
    }

    fun conversations(): List<Conversation> {
        val array = request("/conversations").optJSONArray("conversations") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    Conversation(
                        id = item.getString("id"),
                        participant = User(
                            id = item.getString("participant_id"),
                            name = item.optString("display_name", item.optString("username")),
                            online = false
                        ),
                        lastMessage = item.optString("last_message"),
                        unreadCount = item.optInt("unread_count")
                    )
                )
            }
        }
    }

    fun messages(conversationId: String): List<Message> {
        val array = request("/conversations/$conversationId/messages").optJSONArray("messages") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    Message(
                        id = item.getString("id"),
                        conversationId = item.getString("conversation_id"),
                        senderId = item.getString("sender_id"),
                        text = item.optString("text"),
                        timestamp = item.optLong("created_at"),
                        status = runCatching { MessageStatus.valueOf(item.optString("status", "SENT")) }
                            .getOrDefault(MessageStatus.SENT),
                        deleted = item.optLong("deleted_at") > 0L
                    )
                )
            }
        }
    }
}

class WawProductionChatRepository(
    private val api: WawApiClient,
    endpoint: String,
    tokenProvider: () -> String?,
    private val currentUserId: String,
    client: OkHttpClient = OkHttpClient()
) : ChatRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val socket = WebSocketChatRepository(endpoint, tokenProvider, client)
    private val conversationsState = MutableStateFlow<List<Conversation>>(emptyList())
    private val messagesState = MutableStateFlow<List<Message>>(emptyList())

    init {
        socket.connect()
        refresh()
    }

    fun refresh() {
        scope.launch {
            runCatching {
                val conversations = api.conversations()
                val messages = conversations.flatMap { api.messages(it.id) }
                conversationsState.value = conversations
                messagesState.value = messages
            }
        }
    }

    override fun conversations(): Flow<List<Conversation>> = conversationsState.asStateFlow()

    override fun messages(conversationId: String): Flow<List<Message>> =
        messagesState.map { all -> all.filter { it.conversationId == conversationId } }

    override suspend fun sendMessage(conversationId: String, senderId: String, text: String): Message {
        val message = socket.sendMessage(conversationId, senderId, text)
        messagesState.value = messagesState.value + message
        return message
    }

    override suspend fun markRead(conversationId: String) {
        conversationsState.value = conversationsState.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        messagesState.value = messagesState.value.map {
            if (it.id == messageId && it.senderId == currentUserId) it.copy(deleted = true, text = "Pesan dihapus") else it
        }
    }
}
