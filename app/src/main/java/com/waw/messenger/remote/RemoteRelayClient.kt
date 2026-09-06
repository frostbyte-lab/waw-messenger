package com.waw.messenger.remote

import android.graphics.Bitmap
import android.media.Image
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RemoteRelayClient(
    private val relayUrl: String,
    private val pairingCode: String,
    private val onApproved: () -> Unit = {},
    private val onInputCommand: (String) -> Unit = {},
    private val onClosed: () -> Unit = {}
) {
    private val client = OkHttpClient()
    private var socket: WebSocket? = null
    private var sequence = 0L

    fun connect() {
        require(relayUrl.startsWith("wss://")) { "Remote relay must use wss://" }
        socket = client.newWebSocket(Request.Builder().url(relayUrl).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("{\"type\":\"host\",\"code\":\"$pairingCode\"}")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                when {
                    text.contains("\"type\":\"host-ready\"") -> {
                        // MediaProjection permission was explicitly granted by the User.
                        webSocket.send("{\"type\":\"approve\"}")
                    }
                    text.contains("\"type\":\"approved\"") -> onApproved()
                    text.contains("\"type\":\"input-command\"") -> onInputCommand(text)
                }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { onClosed() }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { onClosed() }
        })
    }

    fun sendImage(image: Image) {
        val plane = image.planes.firstOrNull() ?: return
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, output)
        val payload = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        val message = "{\"type\":\"screen-frame\",\"sequence\":${sequence++},\"width\":${image.width},\"height\":${image.height},\"payloadBase64\":\"$payload\"}"
        socket?.send(message)
        bitmap.recycle()
    }

    fun close() { socket?.close(1000, "user disconnected"); client.dispatcher.executorService.shutdown() }
}
