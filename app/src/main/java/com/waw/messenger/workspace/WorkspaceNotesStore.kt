package com.waw.messenger.workspace

import android.content.Context

/** Local WAW-owned notes/tasks; never connected to WhatsApp data. */
class WorkspaceNotesStore(context: Context) {
    private val store = com.waw.messenger.security.SecureStore(context)

    fun list(): List<String> = store.get(KEY).orEmpty().lines().filter { it.isNotBlank() }.takeLast(100).reversed()

    fun add(text: String) {
        val value = text.trim()
        if (value.isEmpty()) return
        val next = (store.get(KEY).orEmpty() + value + "\n").takeLast(MAX_BYTES)
        store.put(KEY, next)
    }

    private companion object {
        const val KEY = "workspace_notes_tasks"
        const val MAX_BYTES = 64_000
    }
}
