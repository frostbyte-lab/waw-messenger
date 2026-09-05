package com.waw.messenger.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.waw.messenger.auth.AuthRepository
import com.waw.messenger.chat.LiveChatRepository

/** Background connectivity check and authenticated WAW chat synchronization. */
class NetworkSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val auth = AuthRepository(applicationContext)
        val token = auth.token()
        if (token.isNullOrBlank()) return Result.success()

        val chat = LiveChatRepository(auth.baseUrl, token)
        return try {
            auth.me()
            val conversations = chat.conversations()
            conversations.forEach { conversation ->
                chat.messages(conversation.id)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            chat.close()
        }
    }
}
