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
        WorkspaceFeature("watermark", "Custom Watermark", "Tambahkan watermark teks atau gambar ke output.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("attendance", "Fingerprint Attendance", "Pencatatan absensi menggunakan sidik jari; bukan kunci Workspace atau Vault.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("network", "Network Diagnostics", "Informasi IP dan diagnostik jaringan.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("shield", "WAW Shield", "Perlindungan anti-phishing dan blocklist berisiko.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("notes", "Notes & Tasks", "Catatan, checklist, dan tugas lokal milik WAW.", WorkspaceFeatureStatus.IMPLEMENTED),
        WorkspaceFeature("backup", "Backup & Sync", "Backup, restore, dan sinkronisasi data WAW-owned.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("remote", "Remote PC / Android", "Pairing dan remote control perangkat dengan otorisasi eksplisit.", WorkspaceFeatureStatus.NOT_STARTED),
        WorkspaceFeature("search", "Universal Search", "Pencarian rekursif lintas folder Workspace milik WAW.", WorkspaceFeatureStatus.IMPLEMENTED)
    )
}
