package com.waw.userremote.workspace

import android.content.Context
import java.security.SecureRandom

class RemoteSessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_remote_session", Context.MODE_PRIVATE)
    fun generatePairingCode(): String { val code = (SecureRandom().nextInt(900000) + 100000).toString(); prefs.edit().putString("code", code).putLong("expires", System.currentTimeMillis() + 120_000).apply(); return code }
    fun currentCode(): String = prefs.getString("code", "").orEmpty().takeIf { prefs.getLong("expires", 0) > System.currentTimeMillis() }.orEmpty()
    fun revoke() { prefs.edit().remove("code").remove("expires").apply() }
}
