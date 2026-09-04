package com.waw.messenger.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

class WebSocketChatRepository(
    private val endpoint: String,
    private val client: OkHttpClient = OkHttpClient()
) : ChatRepository {
    private val messagesState = MutableStateFlow<List<Message>>(emptyList())
    private val conversationsState = MutableStateFlow<List<Conversation>>(emptyList())
    private var socket: WebSocket? = null

    fun connect() {
        socket = client.newWebSocket(Request.Builder().url(endpoint).build(), object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                if (json.optString("type") != "message") return
                val message = Message(
                    id = json.optString("id"),
                    conversationId = json.optString("conversationId"),
                    senderId = json.optString("senderId"),
                    text = json.optString("text"),
                    status = MessageStatus.valueOf(json.optString("status", "SENT"))
                )
                messagesState.value = messagesState.value + message
            }
        })
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
            throw IllegalStateException("WebSocket tidak terhubung")
        }
        return message.copy(status = MessageStatus.SENT)
    }

    override suspend fun markRead(conversationId: String) {}
    override suspend fun deleteMessage(messageId: String) {}
    fun close() { socket?.close(1000, "closed") }
}
