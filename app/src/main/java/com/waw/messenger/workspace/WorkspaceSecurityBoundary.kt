package com.waw.messenger.workspace

/**
 * Central rules for WAW-owned Workspace features.
 * This class deliberately contains no WhatsApp credentials or session handling.
 */
object WorkspaceSecurityBoundary {
    const val DATA_OWNER = "WAW"

    fun allowsWorkspaceData(): Boolean = true

    fun allowsWhatsAppPrivateCredentialStorage(): Boolean = false

    fun allowsUndocumentedWhatsAppProtocol(): Boolean = false

    fun requiresExplicitRemoteAuthorization(): Boolean = true

    fun ipGeolocationIsApproximate(): Boolean = true
}
