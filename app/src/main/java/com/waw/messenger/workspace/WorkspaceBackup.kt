package com.waw.messenger.workspace

import android.content.Context
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WorkspaceBackup {
    data class Payload(val notes: List<String>, val attendance: List<String>)

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

    fun read(context: Context, uri: Uri): Payload? = runCatching {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return null
        Payload(array(text, "notes"), array(text, "attendance"))
    }.getOrNull()

    private fun array(json: String, key: String): List<String> {
        val body = Regex("\\\"$key\\\"\\s*:\\s*\\[(.*?)]", RegexOption.DOT_MATCHES_ALL).find(json)?.groupValues?.get(1).orEmpty()
        return Regex("\\\"((?:\\\\.|[^\\\"])*)\\\"").findAll(body).map { it.groupValues[1].replace("\\\\n", "\n").replace("\\\\\"", "\"").replace("\\\\\\", "\\") }.toList()
    }
}
