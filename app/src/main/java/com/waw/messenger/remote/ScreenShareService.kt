package com.waw.messenger.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.waw.messenger.R

/** Keeps the user-visible screen-share session alive after Android consent. */
class ScreenShareService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(NOTIFICATION_ID, notification())
        // Transport/encoder is intentionally not started until a paired peer is authenticated.
        return START_NOT_STICKY
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

    companion object { private const val CHANNEL_ID = "waw_remote"; private const val NOTIFICATION_ID = 7401 }
}
