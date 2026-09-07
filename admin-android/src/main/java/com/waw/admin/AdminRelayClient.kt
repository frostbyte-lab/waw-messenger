package com.waw.admin

import android.graphics.BitmapFactory
import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.security.MessageDigest

class AdminRelayClient {
    private val http = OkHttpClient()
    private var socket: WebSocket? = null
    private var sessionId: String? = null
    private val _status = MutableStateFlow("DISCONNECTED")
    val status: StateFlow<String> = _status
    private val _frame = MutableStateFlow<android.graphics.Bitmap?>(null)
    val frame: StateFlow<android.graphics.Bitmap?> = _frame

    fun connect(relayUrl: String, otp: String) {
        disconnect()
        if (!relayUrl.startsWith("wss://")) {
            _status.value = "SECURE_RELAY_REQUIRED"
            return
        }
        _status.value = "CONNECTING"
        val request = Request.Builder().url(relayUrl).build()
        socket = http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(JSONObject().put("type", "viewer").put("code", otp).toString())
                _status.value = "PAIRING"
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = runCatching { JSONObject(text) }.getOrNull() ?: return
                when (message.optString("type")) {
                    "viewer-ready" -> { sessionId = message.optString("sessionId"); _status.value = "WAITING_FOR_USER_APPROVAL" }
                    "user-consent" -> { sessionId = message.optString("sessionId", sessionId.orEmpty()); _status.value = "READY_FOR_OPERATOR_APPROVAL" }
                    "approved" -> _status.value = "CONNECTED"
                    "screen-frame" -> {
                        if (_status.value != "CONNECTED") return
                        val bytes = runCatching {
                            Base64.decode(message.getString("payloadBase64"), Base64.DEFAULT)
                        }.getOrNull() ?: return
                        _frame.value = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _status.value = "DISCONNECTED"
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _status.value = "CONNECTION_ERROR"
            }
        })
    }

    fun approve() {
        if (_status.value == "READY_FOR_OPERATOR_APPROVAL" || _status.value == "WAITING_FOR_USER_APPROVAL") {
            socket?.send(JSONObject().put("type", "approve").put("sessionId", sessionId).toString())
        }
    }

    fun sendTouch(x: Float, y: Float) {
        send(JSONObject().put("type", "input-command").put("sessionId", sessionId).put("capability", "TOUCH_INPUT").put("inputType", "TOUCH_DOWN").put("x", x).put("y", y))
    }

    fun sendKey(keyCode: Int) {
        send(JSONObject().put("type", "input-command").put("sessionId", sessionId).put("capability", "KEYBOARD_INPUT").put("inputType", "KEY_DOWN").put("keyCode", keyCode))
    }

    fun sendSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long = 350L) {
        send(JSONObject().put("type", "input-command").put("sessionId", sessionId).put("capability", "TOUCH_INPUT").put("inputType", "SWIPE").put("x1", x1).put("y1", y1).put("x2", x2).put("y2", y2).put("durationMs", durationMs.coerceIn(80L, 1500L)))
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        send(JSONObject().put("type", "input-command").put("sessionId", sessionId).put("capability", "KEYBOARD_INPUT").put("inputType", "TEXT_INPUT").put("text", text.take(4096)))
    }

    fun sendApprovedAction(action: String) {
        if (action !in setOf("BACK", "HOME", "RECENTS", "NOTIFICATION_SHADE")) return
        send(JSONObject().put("type", "input-command").put("sessionId", sessionId).put("capability", "APPROVED_ACTIONS").put("inputType", "APPROVED_ACTION").put("action", action))
    }

    fun requestOpenApp(packageName: String, label: String) {
        if (packageName !in APP_ALLOWLIST || _status.value != "CONNECTED") return
        send(JSONObject().put("type", "app-request").put("sessionId", sessionId).put("capability", "APP_ACCESS").put("requestId", java.util.UUID.randomUUID().toString()).put("packageName", packageName).put("label", label))
    }

    fun sendFile(resolver: ContentResolver, uri: Uri): Boolean {
        if (_status.value != "CONNECTED") return false
        val bytes = runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return false
        if (bytes.size > MAX_FILE_BYTES) return false
        val name = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else "remote-file.bin" } ?: "remote-file.bin"
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        return socket?.send(JSONObject().put("type", "file-offer").put("sessionId", sessionId).put("capability", "FILE_TRANSFER").put("name", name.take(120)).put("size", bytes.size).put("sha256", digest).put("payloadBase64", Base64.encodeToString(bytes, Base64.NO_WRAP)).toString()) == true
    }

    fun disconnect() {
        socket?.send(JSONObject().put("type", "disconnect").toString())
        socket?.close(1000, "admin disconnected")
        socket = null
        _status.value = "DISCONNECTED"
        _frame.value = null
    }

    private fun send(message: JSONObject) {
        if (_status.value == "CONNECTED") socket?.send(message.toString())
    }

    companion object {
        const val MAX_FILE_BYTES = 5 * 1024 * 1024
        val APP_ALLOWLIST = setOf("com.whatsapp", "com.facebook.katana", "com.zhiliaoapp.musically", "com.instagram.android", "org.telegram.messenger", "com.google.android.youtube", "com.android.chrome", "com.android.settings")
    }
}
