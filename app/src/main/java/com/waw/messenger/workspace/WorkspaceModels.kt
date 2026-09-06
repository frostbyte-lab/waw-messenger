package com.waw.messenger.workspace

/** WAW-owned Workspace feature state. */
enum class WorkspaceFeatureStatus {
    NOT_STARTED,
    IN_PROGRESS,
    IMPLEMENTED,
    BLOCKED,
    LOCKED
}

data class WorkspaceFeature(
    val id: String,
    val title: String,
    val description: String,
    val status: WorkspaceFeatureStatus
)

object WorkspaceCatalog {
    val features = listOf(
        WorkspaceFeature("file_manager", "File Manager", "Browse, folder operations, rename, copy, move, delete, share, and search pada lokasi yang diizinkan pengguna.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("documents", "Documents & PDF", "Editor TXT/Markdown dan ekspor dokumen ke PDF.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("scanner", "Camera Scanner", "Scan dokumen dan ekspor ke PDF.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("watermark", "WAW Watermark", "Watermark resmi WAW dengan kolom kustom, timestamp, lokasi, kompas, dan logo brand pengguna. Branding WAW serta Made by Frostbyte Tech Ltd selalu dipertahankan.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("security", "Biometric & Vault", "Kunci Workspace dan penyimpanan aman.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("network", "Network Diagnostics", "Informasi IP dan diagnostik jaringan.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("shield", "WAW Shield", "Perlindungan anti-phishing dan blocklist berisiko.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("notes", "Notes & Tasks", "Catatan, checklist, dan tugas.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("backup", "Backup & Sync", "Backup, restore, dan sinkronisasi data WAW-owned.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("remote", "Remote PC / Android", "Pairing dan remote control perangkat dengan otorisasi eksplisit.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("search", "Universal Search", "Pencarian lintas data Workspace milik WAW.", WorkspaceFeatureStatus.NOT_STARTED)
    )
}
