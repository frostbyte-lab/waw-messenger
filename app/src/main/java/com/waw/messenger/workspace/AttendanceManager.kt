package com.waw.messenger.workspace

import android.content.Context
import com.waw.messenger.security.SecureStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Stores only local attendance timestamps; it never stores fingerprint data. */
class AttendanceManager(context: Context) {
    private val store = SecureStore(context)
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

    fun record(type: String): String {
        val timestamp = formatter.format(Date())
        val existing = store.get(KEY).orEmpty()
        store.put(KEY, (existing + "$timestamp|$type\n").takeLast(MAX_BYTES))
        return timestamp
    }

    fun recent(): List<String> = store.get(KEY).orEmpty().lines().filter { it.isNotBlank() }.takeLast(20).reversed()

    private companion object {
        const val KEY = "attendance_records"
        const val MAX_BYTES = 32_000
    }
}
