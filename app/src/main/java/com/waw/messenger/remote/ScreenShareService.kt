package com.waw.messenger.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.waw.messenger.R

/** User-visible Android screen capture host. A paired transport can consume frames later. */
class ScreenShareService : Service() {
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var relay: RemoteRelayClient? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(NOTIFICATION_ID, notification())
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        val relayUrl = intent?.getStringExtra(EXTRA_RELAY_URL).orEmpty()
        if (relayUrl.startsWith("wss://") && relay == null) {
            relay = RemoteRelayClient(relayUrl, RemoteSessionManager(this).currentCode())
            relay?.connect()
        }
        if (resultCode != 0 && data != null && projection == null) startCapture(resultCode, data)
        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        val manager = getSystemService(MediaProjectionManager::class.java)
        projection = manager.getMediaProjection(resultCode, data)
        val metrics = resources.displayMetrics
        reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
        reader?.setOnImageAvailableListener({ imageReader ->
            imageReader.acquireLatestImage()?.also { image ->
                relay?.sendImage(image)
                image.close()
            }
        }, null)
        display = projection?.createVirtualDisplay(
            "WAW-Remote-Screen",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader?.surface,
            null,
            null
        )
    }

    override fun onDestroy() {
        display?.release()
        reader?.close()
        projection?.stop()
        relay?.close()
        super.onDestroy()
    }

    private fun notification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_waw_logo)
        .setContentTitle("WAW Remote aktif")
        .setContentText("Screen sharing aktif — buka WAW untuk memutuskan")
        .setOngoing(true)
        .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "WAW Remote", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_RESULT_CODE = "waw.remote.resultCode"
        const val EXTRA_RESULT_DATA = "waw.remote.resultData"
        const val EXTRA_RELAY_URL = "waw.remote.relayUrl"
        private const val CHANNEL_ID = "waw_remote"
        private const val NOTIFICATION_ID = 7401
    }
}
