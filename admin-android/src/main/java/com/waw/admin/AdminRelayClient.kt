package com.waw.admin

import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class AdminRelayClient {
    private val http = OkHttpClient()
    private var socket: WebSocket? = null
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
                    "viewer-ready" -> _status.value = "WAITING_FOR_USER_APPROVAL"
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

    fun sendTouch(x: Float, y: Float) {
        send(JSONObject().put("type", "input-command").put("inputType", "TOUCH_DOWN").put("x", x).put("y", y))
    }

    fun sendKey(keyCode: Int) {
        send(JSONObject().put("type", "input-command").put("inputType", "KEY_DOWN").put("keyCode", keyCode))
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
}
