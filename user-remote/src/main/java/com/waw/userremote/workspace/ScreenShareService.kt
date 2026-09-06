package com.waw.userremote.workspace

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import androidx.core.app.NotificationCompat

class ScreenShareService : Service() {
    private var projection: MediaProjection? = null
    private var display: android.hardware.display.VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var relay: RemoteRelayClient? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(NOTIFICATION_ID, notification())
        if (intent?.action == ACTION_REVOKE) { stopSelf(); return START_NOT_STICKY }
        val code = intent?.getStringExtra(EXTRA_CODE).orEmpty()
        val relayUrl = intent?.getStringExtra(EXTRA_RELAY_URL).orEmpty()
        val caps = intent?.getStringArrayListExtra(EXTRA_CAPABILITIES)?.toSet().orEmpty()
        if (relay == null && code.isNotBlank()) {
            relay = RemoteRelayClient(relayUrl, code, caps, { state -> sendBroadcast(Intent(ACTION_STATE).putExtra(EXTRA_STATE, state)) }, { raw -> RemoteInputService.dispatch(raw) })
            relay?.connect()
        }
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (resultCode != 0 && data != null && projection == null) startCapture(resultCode, data)
        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        projection = getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, data)
        val metrics = resources.displayMetrics
        reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
        reader?.setOnImageAvailableListener({ source -> source.acquireLatestImage()?.use { relay?.sendImage(it) } }, Handler(Looper.getMainLooper()))
        display = projection?.createVirtualDisplay("WAW-User-Remote", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, null)
    }

    override fun onDestroy() { RemoteInputService.activate(false); relay?.revoke(); display?.release(); reader?.close(); projection?.stop(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Remote aktif")
        .setContentText("Screen sharing aktif — Revoke menghentikan semua akses")
        .setOngoing(true).addAction(NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "REVOKE", PendingIntent.getService(this, 1, Intent(this, ScreenShareService::class.java).setAction(ACTION_REVOKE), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)).build()).build()
    private fun createChannel() { if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "Remote session", NotificationManager.IMPORTANCE_LOW)) }

    companion object {
        const val ACTION_REVOKE = "com.waw.userremote.REVOKE"
        const val ACTION_STATE = "com.waw.userremote.STATE"
        const val EXTRA_STATE = "state"
        const val EXTRA_CODE = "code"
        const val EXTRA_RELAY_URL = "relayUrl"
        const val EXTRA_CAPABILITIES = "capabilities"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        private const val CHANNEL_ID = "user_remote_session"
        private const val NOTIFICATION_ID = 8701
    }
}
