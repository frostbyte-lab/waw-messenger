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

    fun list(parentUri: Uri): List<WorkspaceFileItem> =
        DocumentFile.fromTreeUri(context, parentUri)?.listFiles()
            ?.map { file -> item(file) }
            ?.sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: DocumentFile.fromSingleUri(context, parentUri)?.let { parent ->
                if (!parent.isDirectory) emptyList() else parent.listFiles().map { item(it) }
            }?.sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()

    private fun item(file: DocumentFile) = WorkspaceFileItem(
        uri = file.uri,
        name = file.name ?: "Unnamed",
        isDirectory = file.isDirectory,
        sizeBytes = if (file.isFile) file.length().takeIf { it >= 0L } else null,
        canWrite = file.canWrite()
    )

    fun createFolder(parentUri: Uri, name: String): Uri? {
        val parent = DocumentFile.fromTreeUri(context, parentUri)
            ?: DocumentFile.fromSingleUri(context, parentUri)
        return parent?.createDirectory(name.trim())?.uri
    }

    fun createFile(parentUri: Uri, mimeType: String, name: String): Uri? {
        val parent = DocumentFile.fromTreeUri(context, parentUri)
            ?: DocumentFile.fromSingleUri(context, parentUri)
        return parent?.createFile(mimeType, name.trim())?.uri
    }

    fun rename(uri: Uri, newName: String): Uri? = runCatching {
        DocumentsContract.renameDocument(resolver, uri, newName.trim())
    }.getOrNull()

    fun delete(uri: Uri): Boolean = runCatching {
        DocumentsContract.deleteDocument(resolver, uri)
        true
    }.getOrDefault(false)

    fun copy(uri: Uri, targetTreeUri: Uri): Uri? = runCatching {
        val sourceDoc = DocumentsContract.getDocumentId(uri)
        val targetDoc = DocumentsContract.getTreeDocumentId(targetTreeUri)
        DocumentsContract.copyDocument(resolver, uri, DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, targetDoc))
    }.getOrNull()

    fun move(uri: Uri, targetTreeUri: Uri): Uri? = runCatching {
        val targetDoc = DocumentsContract.getTreeDocumentId(targetTreeUri)
        DocumentsContract.moveDocument(
            resolver,
            uri,
            DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, targetDoc),
            DocumentsContract.buildDocumentUriUsingTree(targetTreeUri, targetDoc)
        )
    }.getOrNull()

    fun readText(uri: Uri): String? = runCatching {
        resolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
    }.getOrNull()

    fun writeText(uri: Uri, text: String): Boolean = runCatching {
        resolver.openFileDescriptor(uri, "w")?.use { pfd ->
            FileOutputStream(pfd.fileDescriptor).use { output -> output.write(text.toByteArray(Charsets.UTF_8)) }
        } != null
    }.getOrDefault(false)

    fun openStream(uri: Uri) = resolver.openInputStream(uri)

    fun displayName(uri: Uri): String? = DocumentFile.fromSingleUri(context, uri)?.name
}

data class WorkspaceFileItem(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long?,
    val canWrite: Boolean = false
)
