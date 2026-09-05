package com.waw.messenger.workspace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceSecurityBoundaryTest {
    @Test
    fun workspaceOwnsItsDataButNeverStoresWhatsAppPrivateCredentials() {
        assertTrue(WorkspaceSecurityBoundary.allowsWorkspaceData())
        assertFalse(WorkspaceSecurityBoundary.allowsWhatsAppPrivateCredentialStorage())
        assertFalse(WorkspaceSecurityBoundary.allowsUndocumentedWhatsAppProtocol())
        assertTrue(WorkspaceSecurityBoundary.requiresExplicitRemoteAuthorization())
        assertFalse(WorkspaceStoragePolicy.sensitiveWhatsAppDataMayBePersisted())
    }
}
