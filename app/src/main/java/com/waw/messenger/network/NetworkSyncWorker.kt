package com.waw.messenger.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Persistent network work hook for future message/sync queues.
 * It intentionally performs no WhatsApp credential or token handling.
 */
class NetworkSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            // Phase 17 establishes scheduling/retry infrastructure.
            // Actual WAW-owned sync work will be attached in the sync phase.
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
