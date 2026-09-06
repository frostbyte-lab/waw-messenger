package com.waw.messenger.remote

import android.content.Context
import java.security.SecureRandom

class RemoteSessionManager(context: Context) {
    private val store = com.waw.messenger.security.SecureStore(context)

    fun generatePairingCode(): String {
        val code = (SecureRandom().nextInt(900000) + 100000).toString()
        store.put(KEY_CODE, code)
        store.put(KEY_EXPIRES_AT, (System.currentTimeMillis() + PAIRING_WINDOW_MS).toString())
        store.put(KEY_STATUS, "WAITING_FOR_ADMIN")
        return code
    }

    fun currentCode(): String {
        val code = store.get(KEY_CODE).orEmpty()
        val expiry = store.get(KEY_EXPIRES_AT)?.toLongOrNull() ?: 0L
        return if (code.isNotBlank() && expiry > System.currentTimeMillis()) code else ""
    }

    fun status(): String {
        val current = store.get(KEY_STATUS).orEmpty().ifBlank { "DISCONNECTED" }
        return if (current == "WAITING_FOR_ADMIN" && currentCode().isBlank()) "OTP_EXPIRED" else current
    }

    fun expiresAt(): Long = store.get(KEY_EXPIRES_AT)?.toLongOrNull() ?: 0L

    fun revoke() {
        store.put(KEY_CODE, "")
        store.put(KEY_EXPIRES_AT, "0")
        store.put(KEY_STATUS, "REVOKED")
    }

    private companion object {
        const val KEY_CODE = "remote_pairing_code"
        const val KEY_EXPIRES_AT = "remote_pairing_expires_at"
        const val KEY_STATUS = "remote_status"
        const val PAIRING_WINDOW_MS = 120_000L
    }
}
