package com.waw.messenger.network

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RetryPolicyTest {
    @Test
    fun retriesIoFailureUntilSuccess() = runBlocking {
        var attempts = 0

        val result = RetryPolicy.execute(initialDelayMs = 0, maxDelayMs = 0) {
            attempts += 1
            if (attempts < 3) throw IOException("temporary")
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test
    fun stopsAfterMaximumAttempts() = runBlocking {
        var attempts = 0

        assertThrows(IOException::class.java) {
            runBlocking {
                RetryPolicy.execute(maxAttempts = 2, initialDelayMs = 0, maxDelayMs = 0) {
                    attempts += 1
                    throw IOException("persistent")
                }
            }
        }

        assertEquals(2, attempts)
    }
}
