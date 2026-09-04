package com.waw.messenger.workspace

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

/**
 * User-authorized file operations for WAW Workspace.
 * Access is limited to URI trees granted by the Android Storage Access Framework.
 */
class WorkspaceFileManager(private val context: Context) {
    private val resolver: ContentResolver = context.contentResolver

    fun list(treeUri: Uri): List<WorkspaceFileItem> =
        DocumentFile.fromTreeUri(context, treeUri)
            ?.listFiles()
            ?.map { file ->
                WorkspaceFileItem(
                    uri = file.uri,
                    name = file.name ?: "Unnamed",
                    isDirectory = file.isDirectory,
                    sizeBytes = if (file.isFile) file.length() else null
                )
            }
            ?.sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()

    fun createFolder(parentUri: Uri, name: String): Uri? {
        val safeName = name.trim()
        if (safeName.isEmpty()) return null
        return DocumentsContract.createDocument(
            resolver,
            parentUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            safeName
        )
    }

    fun createFile(parentUri: Uri, mimeType: String, name: String): Uri? {
        val safeName = name.trim()
        if (safeName.isEmpty()) return null
        return DocumentsContract.createDocument(resolver, parentUri, mimeType, safeName)
    }

    fun rename(uri: Uri, newName: String): Uri? {
        val safeName = newName.trim()
        if (safeName.isEmpty()) return null
        return DocumentsContract.renameDocument(resolver, uri, safeName)
    }

    fun delete(uri: Uri): Boolean = runCatching {
        DocumentsContract.deleteDocument(resolver, uri)
        true
    }.getOrDefault(false)

    fun displayName(uri: Uri): String? =
        DocumentFile.fromSingleUri(context, uri)?.name
}

data class WorkspaceFileItem(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long?
)
