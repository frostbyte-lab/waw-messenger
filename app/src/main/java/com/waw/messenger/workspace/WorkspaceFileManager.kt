package com.waw.messenger.workspace

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.io.FileOutputStream

/** WAW Workspace file operations limited to user-authorized SAF trees. */
class WorkspaceFileManager(private val context: Context) {
    private val resolver: ContentResolver = context.contentResolver

    fun list(parentUri: Uri): List<WorkspaceFileItem> {
        val parent = DocumentFile.fromTreeUri(context, parentUri)
            ?: DocumentFile.fromSingleUri(context, parentUri)
            ?: return emptyList()
        if (!parent.isDirectory) return emptyList()
        return parent.listFiles().map(::item)
            .sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    fun search(rootUri: Uri, query: String, maxResults: Int = 200): List<WorkspaceFileItem> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return emptyList()
        val results = mutableListOf<WorkspaceFileItem>()
        fun walk(uri: Uri) {
            if (results.size >= maxResults) return
            list(uri).forEach { item ->
                if (item.name.lowercase().contains(needle)) results += item
                if (item.isDirectory) walk(item.uri)
            }
        }
        walk(rootUri)
        return results
    }

    private fun item(file: DocumentFile) = WorkspaceFileItem(
        uri = file.uri,
        name = file.name ?: "Unnamed",
        isDirectory = file.isDirectory,
        sizeBytes = if (file.isFile) file.length().takeIf { it >= 0L } else null,
        canWrite = file.canWrite()
    )

    fun createFolder(parentUri: Uri, name: String): Uri? {
        val safeName = name.trim()
        if (safeName.isEmpty()) return null
        return (DocumentFile.fromTreeUri(context, parentUri) ?: DocumentFile.fromSingleUri(context, parentUri))
            ?.createDirectory(safeName)?.uri
    }

    fun createFile(parentUri: Uri, mimeType: String, name: String): Uri? {
        val safeName = name.trim()
        if (safeName.isEmpty()) return null
        return (DocumentFile.fromTreeUri(context, parentUri) ?: DocumentFile.fromSingleUri(context, parentUri))
            ?.createFile(mimeType, safeName)?.uri
    }

    fun rename(uri: Uri, newName: String): Uri? = runCatching {
        val safeName = newName.trim()
        if (safeName.isEmpty()) null else DocumentsContract.renameDocument(resolver, uri, safeName)
    }.getOrNull()

    fun delete(uri: Uri): Boolean = runCatching {
        DocumentsContract.deleteDocument(resolver, uri)
        true
    }.getOrDefault(false)

    fun copy(uri: Uri, targetTreeUri: Uri): Uri? = runCatching {
        val targetId = DocumentsContract.getTreeDocumentId(targetTreeUri)
        val target = DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, targetId)
        DocumentsContract.copyDocument(resolver, uri, target)
    }.getOrNull()

    /** Move is implemented as copy-then-delete for broad DocumentsProvider compatibility. */
    fun move(uri: Uri, targetTreeUri: Uri): Uri? = runCatching {
        val copied = copy(uri, targetTreeUri) ?: return@runCatching null
        if (delete(uri)) copied else null
    }.getOrNull()

    fun readText(uri: Uri): String? = runCatching {
        resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8).use { it?.readText() }
    }.getOrNull()

    fun writeText(uri: Uri, text: String): Boolean = runCatching {
        resolver.openFileDescriptor(uri, "w")?.use { pfd ->
            FileOutputStream(pfd.fileDescriptor).use { output -> output.write(text.toByteArray(Charsets.UTF_8)) }
        } != null
    }.getOrDefault(false)
}

data class WorkspaceFileItem(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long?,
    val canWrite: Boolean = false
)
