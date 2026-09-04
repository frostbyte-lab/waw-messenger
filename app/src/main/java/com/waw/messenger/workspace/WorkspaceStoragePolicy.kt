package com.waw.messenger.workspace

/** Storage rules for WAW-owned Workspace data. */
object WorkspaceStoragePolicy {
    const val INTERNAL_PRIVATE_DATA = "internal-app-storage"
    const val SHARED_EXPORT = "content-uri-with-grant"

    fun privateDataShouldUseInternalStorage(): Boolean = true

    fun sharedFilesShouldUseContentUri(): Boolean = true

    fun sensitiveWhatsAppDataMayBePersisted(): Boolean = false
}
