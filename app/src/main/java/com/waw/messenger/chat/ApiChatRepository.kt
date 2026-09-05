package com.waw.messenger.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ApiChatRepository(
    private val baseUrl: String,
    private val token: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private fun request(method: String, path: String, body: JSONObject? = null): String = runBlockingIo {
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer $token")
        if (body != null) {
            builder.method(method, body.toString().toRequestBody(jsonType))
        } else {
            builder.method(method, null)
        }
        client.newCall(builder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error(parseError(text, response.code))
            text
        }
    }

    suspend fun users(): List<User> = withContext(Dispatchers.IO) {
        val root = JSONObject(request("GET", "/users"))
        val array = root.optJSONArray("users") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(User(item.getString("id"), item.getString("displayName"), item.optString("status") == "online"))
            }
        }
    }

    suspend fun conversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val root = JSONObject(request("GET", "/conversations"))
        val array = root.optJSONArray("conversations") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val p = item.getJSONObject("participant")
                add(Conversation(
                    id = item.getString("id"),
                    participant = User(p.getString("id"), p.getString("displayName"), p.optString("status") == "online")
                ))
            }
        }
    }

    suspend fun openConversation(participantId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject(request("POST", "/conversations", JSONObject().put("participantId", participantId)))
        root.getJSONObject("conversation").getString("id")
    }

    suspend fun messages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        val root = JSONObject(request("GET", "/conversations/$conversationId/messages"))
        val array = root.optJSONArray("messages") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val status = runCatching { MessageStatus.valueOf(item.optString("status", "SENT")) }.getOrDefault(MessageStatus.SENT)
                add(Message(
                    id = item.getString("id"),
                    conversationId = item.getString("conversationId"),
                    senderId = item.getString("senderId"),
                    text = item.optString("text"),
                    timestamp = item.optLong("createdAt", System.currentTimeMillis()),
                    status = status,
                    deleted = item.optString("deletedAt").isNotBlank() && item.optString("deletedAt") != "null"
                ))
            }
        }
    }

    suspend fun sendMessage(conversationId: String, text: String, clientId: String): Message = withContext(Dispatchers.IO) {
        val root = JSONObject(request("POST", "/conversations/$conversationId/messages", JSONObject()
            .put("text", text)
            .put("clientId", clientId)))
        val item = root.getJSONObject("message")
        Message(
            id = item.getString("id"),
            conversationId = item.getString("conversationId"),
            senderId = item.getString("senderId"),
            text = item.optString("text"),
            timestamp = item.optLong("createdAt", System.currentTimeMillis()),
            status = MessageStatus.SENT
        )
    }

    suspend fun markRead(conversationId: String) = withContext(Dispatchers.IO) {
        request("POST", "/conversations/$conversationId/read")
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        request("DELETE", "/messages/$messageId")
    }

    private fun parseError(raw: String, code: Int): String = runCatching {
        JSONObject(raw).optString("error").ifBlank { "HTTP_$code" }
    }.getOrDefault("HTTP_$code")

    private fun <T> runBlockingIo(block: () -> T): T = block()
}
