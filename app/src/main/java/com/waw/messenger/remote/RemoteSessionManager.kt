package com.waw.messenger.remote

import android.content.Context
import java.security.SecureRandom

class RemoteSessionManager(context: Context) {
    private val store = com.waw.messenger.security.SecureStore(context)
    fun generatePairingCode(): String {
        val code = SecureRandom().nextInt(900000) + 100000
        store.put(KEY_CODE, code.toString())
        store.put(KEY_STATUS, "WAITING_FOR_WINDOWS_APPROVAL")
        return code.toString()
    }
    fun currentCode(): String = store.get(KEY_CODE).orEmpty()
    fun status(): String = store.get(KEY_STATUS).orEmpty().ifBlank { "DISCONNECTED" }
    fun revoke() { store.put(KEY_CODE, ""); store.put(KEY_STATUS, "REVOKED") }
    private companion object { const val KEY_CODE = "remote_pairing_code"; const val KEY_STATUS = "remote_status" }
}
