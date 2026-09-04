package com.waw.messenger.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import kotlin.math.min

class WebSocketChatRepository(
    private val endpoint: String,
    private val client: OkHttpClient = OkHttpClient()
) : ChatRepository {
    private val messagesState = MutableStateFlow<List<Message>>(emptyList())
    private val conversationsState = MutableStateFlow<List<Conversation>>(emptyList())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: WebSocket? = null
    private var closed = false
    private var reconnectAttempt = 0

    @Synchronized
    fun connect() {
        if (closed || socket != null) return
        socket = client.newWebSocket(Request.Builder().url(endpoint).build(), listener)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            reconnectAttempt = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runCatching {
                val json = JSONObject(text)
                if (json.optString("type") != "message") return
                val status = runCatching {
                    MessageStatus.valueOf(json.optString("status", "SENT"))
                }.getOrDefault(MessageStatus.SENT)
                val message = Message(
                    id = json.optString("id"),
                    conversationId = json.optString("conversationId"),
                    senderId = json.optString("senderId"),
                    text = json.optString("text"),
                    status = status
                )
                messagesState.value = messagesState.value + message
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            handleDisconnected(webSocket)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            handleDisconnected(webSocket)
        }
    }

    @Synchronized
    private fun handleDisconnected(webSocket: WebSocket) {
        if (socket !== webSocket) return
        socket = null
        if (closed) return
        val attempt = ++reconnectAttempt
        val delayMs = min(30_000L, 1_000L * (1L shl min(attempt - 1, 5)))
        scope.launch {
            delay(delayMs)
            connect()
        }
    }

    override fun conversations(): Flow<List<Conversation>> = conversationsState

    override fun messages(conversationId: String): Flow<List<Message>> =
        kotlinx.coroutines.flow.map(messagesState) { it.filter { m -> m.conversationId == conversationId } }

    override suspend fun sendMessage(conversationId: String, senderId: String, text: String): Message {
        val message = Message(conversationId = conversationId, senderId = senderId, text = text)
        val payload = JSONObject().apply {
            put("type", "message")
            put("id", message.id)
            put("conversationId", conversationId)
            put("senderId", senderId)
            put("text", text)
        }
        if (!(socket?.send(payload.toString()) ?: false)) {
            connect()
            throw IllegalStateException("WebSocket tidak terhubung; pesan belum dikirim")
        }
        return message.copy(status = MessageStatus.SENT)
    }

    override suspend fun markRead(conversationId: String) {}
    override suspend fun deleteMessage(messageId: String) {}

    @Synchronized
    fun close() {
        closed = true
        socket?.close(1000, "closed")
        socket = null
        scope.cancel()
    }
}
