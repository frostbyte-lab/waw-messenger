package com.waw.messenger.network

import kotlinx.coroutines.delay
import kotlin.math.min

object RetryPolicy {
    suspend fun <T> execute(
        maxAttempts: Int = 4,
        initialDelayMs: Long = 500,
        maxDelayMs: Long = 8_000,
        block: suspend (attempt: Int) -> T
    ): T {
        require(maxAttempts > 0)
        var attempt = 1
        var delayMs = initialDelayMs
        while (true) {
            try {
                return block(attempt)
            } catch (error: Throwable) {
                if (attempt >= maxAttempts) throw error
                delay(delayMs)
                delayMs = min(maxDelayMs, delayMs * 2)
                attempt++
            }
        }
    }
}
