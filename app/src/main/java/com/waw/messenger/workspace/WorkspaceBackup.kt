package com.waw.messenger.workspace

import android.content.Context
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WorkspaceBackup {
    fun write(context: Context, uri: Uri, notes: List<String>, attendance: List<String>): Boolean = runCatching {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).format(Date())
        val json = buildString {
            append("{\n  \"owner\": \"WAW\",\n  \"createdAt\": \"").append(now).append("\",\n")
            append("  \"notes\": ").append(array(notes)).append(",\n")
            append("  \"attendance\": ").append(array(attendance)).append("\n}\n")
        }
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } != null
    }.getOrDefault(false)

    private fun array(values: List<String>): String = values.joinToString(prefix = "[", postfix = "]") { "\"${escape(it)}\"" }
    private fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
