package com.waw.messenger.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.json.JSONObject
import java.util.UUID

class LiveChatRepository(
    private val baseUrl: String,
    private val token: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    private var socket: WebSocket? = null

    private fun url(path: String) = baseUrl.trimEnd('/') + path

    suspend fun users(): List<User> = withContext(Dispatchers.IO) {
        val root = get("/users")
        val array = root.optJSONArray("users") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val x = array.getJSONObject(i)
                add(
                    User(
                        x.getString("id"),
                        x.getString("displayName"),
                        x.optString("status") == "online"
                    )
                )
            }
        }
    }

    suspend fun conversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val root = get("/conversations")
        val array = root.optJSONArray("conversations") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val x = array.getJSONObject(i)
                val p = x.getJSONObject("participant")
                add(
                    Conversation(
                        x.getString("id"),
                        User(
                            p.getString("id"),
                            p.getString("displayName"),
                            p.optString("status") == "online"
                        ),
                        x.optString("lastMessage"),
                        x.optInt("unreadCount")
                    )
                )
            }
        }
    }

    suspend fun openConversation(participantId: String): String = withContext(Dispatchers.IO) {
        post("/conversations", JSONObject().put("participantId", participantId))
            .getJSONObject("conversation")
            .getString("id")
    }

    suspend fun messages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        val root = get("/conversations/$conversationId/messages")
        val array = root.optJSONArray("messages") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val x = array.getJSONObject(i)
                val deletedAt = x.opt("deletedAt")
                val status = runCatching {
                    MessageStatus.valueOf(x.optString("status", "SENT"))
                }.getOrDefault(MessageStatus.SENT)
                add(
                    Message(
                        x.getString("id"),
                        x.getString("conversationId"),
                        x.getString("senderId"),
                        x.optString("text"),
                        x.optLong("createdAt"),
                        status,
                        deleted = deletedAt != null && deletedAt != JSONObject.NULL
                    )
                )
            }
        }
    }

    suspend fun sendMessage(conversationId: String, text: String): String = withContext(Dispatchers.IO) {
        val value = text.trim()
        require(value.isNotEmpty()) { "MESSAGE_EMPTY" }
        val clientId = UUID.randomUUID().toString()
        ensureSocket()
        val payload = JSONObject()
            .put("type", "message")
            .put("id", clientId)
            .put("clientId", clientId)
            .put("conversationId", conversationId)
            .put("text", value)
            .toString()
        val sent = socket?.send(payload) ?: false
        if (!sent) error("MESSAGE_SEND_FAILED")
        clientId
    }

    suspend fun markRead(conversationId: String) = withContext(Dispatchers.IO) {
        post("/conversations/$conversationId/read", JSONObject())
    }

    fun close() {
        socket?.close(1000, "closed")
        socket = null
    }

    @Synchronized
    private fun ensureSocket() {
        if (socket != null) return
        val wsUrl = url("/ws")
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
        socket = client.newWebSocket(
            Request.Builder()
                .url(wsUrl)
                .header("Authorization", "Bearer $token")
                .build(),
            object : WebSocketListener() {
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (socket === webSocket) socket = null
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (socket === webSocket) socket = null
                }
            }
        )
    }

    private fun get(path: String): JSONObject = execute("GET", path, null)

    private fun post(path: String, body: JSONObject): JSONObject = execute("POST", path, body)

    private fun execute(method: String, path: String, body: JSONObject?): JSONObject {
        val builder = Request.Builder()
            .url(url(path))
            .header("Authorization", "Bearer $token")
        if (body != null) {
            builder.method(
                method,
                body.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
        } else {
            builder.method(method, null)
        }
        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("HTTP_${response.code}")
            return JSONObject(raw)
        }
    }
}
