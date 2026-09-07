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
                    text.contains("\"type\":\"host-ready\"") -> Unit
                    text.contains("\"type\":\"approved\"") -> onApproved()
                    text.contains("\"type\":\"input-command\"") -> onInputCommand(text)
                }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { onClosed() }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { onClosed() }
        })
    }

    fun sendImage(image: Image) {
        val ws = socket ?: return
        if (ws.queueSize() > 2L * 1024L * 1024L) return
        val plane = image.planes.firstOrNull() ?: return
        val pixelStride = plane.pixelStride.coerceAtLeast(1)
        val rowStride = plane.rowStride.coerceAtLeast(image.width * pixelStride)
        val paddedWidth = (rowStride / pixelStride).coerceAtLeast(image.width)
        val bitmap = runCatching {
            val full = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
            full.copyPixelsFromBuffer(plane.buffer)
            if (paddedWidth == image.width) full else Bitmap.createBitmap(full, 0, 0, image.width, image.height).also { full.recycle() }
        }.getOrNull() ?: return
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, output)
        val payload = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        val message = "{\"type\":\"screen-frame\",\"sequence\":${sequence++},\"width\":${image.width},\"height\":${image.height},\"payloadBase64\":\"$payload\"}"
        ws.send(message)
        bitmap.recycle()
    }

    fun close() { socket?.close(1000, "user disconnected"); client.dispatcher.executorService.shutdown() }
}
