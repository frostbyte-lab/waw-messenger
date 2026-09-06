package com.waw.messenger.workspace

import android.content.Context

class WorkspaceCalendarStore(context: Context) {
    private val store = com.waw.messenger.security.SecureStore(context)
    fun list(): List<String> = store.get(KEY).orEmpty().lines().filter { it.isNotBlank() }.takeLast(100).reversed()
    fun add(event: String) {
        val value = event.trim()
        if (value.isNotEmpty()) store.put(KEY, (store.get(KEY).orEmpty() + value + "\n").takeLast(MAX_BYTES))
    }
    private companion object { const val KEY = "workspace_calendar_events"; const val MAX_BYTES = 32_000 }
}
