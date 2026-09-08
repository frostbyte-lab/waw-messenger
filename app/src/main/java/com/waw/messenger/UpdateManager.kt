package com.waw.messenger

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal data class WawUpdate(
    val version: String,
    val downloadUrl: String,
    val releaseUrl: String,
)

internal object UpdateManager {
    private const val RELEASES_API = "https://api.github.com/repos/frostbyte-lab/waw-messenger/releases/latest"
    private const val APK_ASSET_NAME = "waw-release.apk"
    private val client = OkHttpClient()

    suspend fun findUpdate(): WawUpdate? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(RELEASES_API)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "WAW-Android-Updater")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val release = JSONObject(response.body?.string().orEmpty())
                if (release.optBoolean("draft") || release.optBoolean("prerelease")) {
                    return@withContext null
                }

                val latest = normalize(release.optString("tag_name"))
                val current = normalize(BuildConfig.VERSION_NAME)
                val apkUrl = release.optJSONArray("assets")?.let { assets ->
                    (0 until assets.length())
                        .map { assets.getJSONObject(it) }
                        .firstOrNull { it.optString("name") == APK_ASSET_NAME }
                        ?.optString("browser_download_url")
                }.orEmpty()

                if (isNewer(latest, current) && apkUrl.startsWith("https://github.com/")) {
                    WawUpdate(
                        version = latest.joinToString("."),
                        downloadUrl = apkUrl,
                        releaseUrl = release.optString("html_url"),
                    )
                } else null
            }
        }.getOrNull()
    }

    fun downloadAndInstall(context: Context, update: WawUpdate) {
        val filename = "WAW-${update.version}.apk"
        val request = DownloadManager.Request(Uri.parse(update.downloadUrl))
            .setTitle("WAW ${update.version}")
            .setDescription("Mengunduh update resmi dari GitHub")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return
                try {
                    if (manager.getUriForDownloadedFile(downloadId) != null) {
                        manager.getUriForDownloadedFile(downloadId)?.let { apkUri ->
                            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(apkUri, "application/vnd.android.package-archive")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            receiverContext.startActivity(installIntent)
                        }
                    }
                } finally {
                    receiverContext.unregisterReceiver(this)
                }
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun isNewer(latest: List<Int>, current: List<Int>): Boolean =
        latest.zip(current).firstOrNull { it.first != it.second }?.let { it.first > it.second } ?: false

    private fun normalize(version: String): List<Int> = version
        .removePrefix("v")
        .split(".", "-", "+")
        .take(3)
        .map { it.toIntOrNull() ?: 0 }
        .let { it + List(3 - it.size) { 0 } }
}

internal fun Context.openReleasePage(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
