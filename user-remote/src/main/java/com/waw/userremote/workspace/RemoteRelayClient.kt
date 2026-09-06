package com.waw.userremote.workspace

import android.graphics.Bitmap
import android.media.Image
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class RemoteRelayClient(
    private val relayUrl: String,
    private val pairingCode: String,
    private val capabilities: Set<String>,
    private val onState: (String) -> Unit,
    private val onInput: (String) -> Unit,
) {
    private val client = OkHttpClient.Builder().pingInterval(20, TimeUnit.SECONDS).build()
    private var socket: WebSocket? = null
    private var sessionId: String? = null
    private var approved = false
    private var sequence = 0L

    fun connect() {
        if (!relayUrl.startsWith("wss://")) { onState("SECURE_RELAY_REQUIRED"); return }
        socket = client.newWebSocket(Request.Builder().url(relayUrl).build(), object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send(JSONObject().put("type", "host").put("code", pairingCode).toString())
                onState("WAITING_FOR_OPERATOR")
            }
            override fun onMessage(ws: WebSocket, text: String) {
                val msg = runCatching { JSONObject(text) }.getOrNull() ?: return
                when (msg.optString("type")) {
                    "host-ready", "pair-request" -> {
                        sessionId = msg.optString("sessionId", sessionId.orEmpty())
                        ws.send(JSONObject().put("type", "user-consent").put("sessionId", sessionId).put("capabilities", capabilities).toString())
                        onState("WAITING_FOR_OPERATOR_APPROVAL")
                    }
                    "approved" -> { if (msg.optString("sessionId") == sessionId) { approved = true; onState("ACTIVE") } }
                    "input-command" -> if (approved && msg.optString("sessionId") == sessionId) onInput(text)
                    "revoked", "session-closed" -> { approved = false; onState("REVOKED"); close(false) }
                }
            }
            override fun onClosed(ws: WebSocket, code: Int, reason: String) { approved = false; onState("DISCONNECTED") }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) { approved = false; onState("CONNECTION_ERROR") }
        })
    }

    fun sendImage(image: Image) {
        if (!approved || !capabilities.contains("SCREEN_SHARE")) return
        val plane = image.planes.firstOrNull() ?: return
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        runCatching { bitmap.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(bytes)) }.onFailure { bitmap.recycle(); return }
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, output)
        val payload = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        socket?.send(JSONObject().put("type", "screen-frame").put("sessionId", sessionId).put("sequence", sequence++).put("width", image.width).put("height", image.height).put("payloadBase64", payload).toString())
        bitmap.recycle()
    }

    fun revoke() {
        socket?.send(JSONObject().put("type", "disconnect").put("sessionId", sessionId).toString())
        close(true)
    }

    fun close(notify: Boolean = true) {
        approved = false
        socket?.close(1000, if (notify) "user revoked" else "session closed")
        socket = null
        sessionId = null
        if (notify) onState("REVOKED")
    }
}
