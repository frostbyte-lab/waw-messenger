package com.waw.messenger.workspace

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Fingerprint
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceShell(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val manager = remember(context) { WorkspaceFileManager(context) }
    var rootUri by remember { mutableStateOf<Uri?>(null) }
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var backStack by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var files by remember { mutableStateOf(emptyList<WorkspaceFileItem>()) }
    var query by remember { mutableStateOf("") }
    var editorUri by remember { mutableStateOf<Uri?>(null) }
    var editorName by remember { mutableStateOf("") }
    var editorText by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<FileDialog?>(null) }
    var menuUri by remember { mutableStateOf<Uri?>(null) }
    var pendingTransfer by remember { mutableStateOf<Transfer?>(null) }
    var attendanceDialog by remember { mutableStateOf(false) }
    val attendanceManager = remember(context) { AttendanceManager(context) }
    var notesDialog by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }
    val notesStore = remember(context) { WorkspaceNotesStore(context) }
    var noteItems by remember { mutableStateOf(notesStore.list()) }

    fun refresh(uri: Uri) {
        currentUri = uri
        files = manager.list(uri)
        query = ""
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            rootUri = uri
            backStack = emptyList()
            refresh(uri)
        }
    }

    val transferPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { target ->
        val operation = pendingTransfer
        pendingTransfer = null
        if (target != null && operation != null) {
            val result = if (operation.move) manager.move(operation.uri, target) else manager.copy(operation.uri, target)
            notice = if (result != null) if (operation.move) "Dipindahkan" else "Disalin" else "Operasi gagal"
            currentUri?.let(::refresh)
        }
    }

    val createPdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            val ok = WorkspaceDocumentTools.exportTextToPdf(context, uri, editorName, editorText)
            notice = if (ok) "PDF berhasil dibuat" else "Gagal membuat PDF"
        }
    }

    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            notice = if (WorkspaceBackup.write(context, uri, noteItems, attendanceManager.recent())) "Backup WAW berhasil dibuat" else "Backup gagal dibuat"
        }
    }

    if (editorUri != null) {
        DocumentEditor(
            name = editorName,
            text = editorText,
            onTextChange = { editorText = it },
            onBack = { editorUri = null },
            onSave = {
                val ok = manager.writeText(editorUri!!, editorText)
                notice = if (ok) "Dokumen disimpan" else "Gagal menyimpan"
            },
            onExportPdf = { createPdfPicker.launch(if (editorName.endsWith(".pdf", true)) "document.pdf" else editorName.substringBeforeLast('.') + ".pdf") },
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (currentUri == null) "WAW Workspace" else "File Manager") },
                navigationIcon = {
                    if (currentUri != null) IconButton(onClick = {
                        if (backStack.isNotEmpty()) {
                            val parent = backStack.last()
                            backStack = backStack.dropLast(1)
                            refresh(parent)
                        } else {
                            currentUri = null
                            files = emptyList()
                        }
                    }) { Icon(Icons.Default.ArrowBack, "Kembali") }
                },
                actions = {
                    if (currentUri != null) IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, currentUri.toString())
                        }
                        context.startActivity(Intent.createChooser(intent, "Bagikan lokasi"))
                    }) { Icon(Icons.Default.Share, "Bagikan") }
                }
            )
        },
        floatingActionButton = {
            if (currentUri != null) FloatingActionButton(onClick = { dialog = FileDialog.CreateMenu }) {
                Icon(Icons.Default.Add, "Tambah")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (currentUri == null) {
                Text("WAW Workspace", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
                Text("File Manager + Documents & PDF", modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                Button(onClick = { folderPicker.launch(null) }, modifier = Modifier.fillMaxWidth()) { Text("Pilih Folder") }
                OutlinedButton(onClick = { attendanceDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Text("  Fingerprint Attendance")
                }
                OutlinedButton(onClick = { notesDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text("  Notes & Tasks")
                }
                OutlinedButton(onClick = { backupPicker.launch("waw-workspace-backup.json") }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("  Export Backup Lokal")
                }
                Text("Akses hanya ke folder yang kamu pilih melalui Android Storage Access Framework.", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall)
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    label = { Text("Cari file / folder") },
                    singleLine = true
                )
                val visible = if (query.isBlank() || rootUri == null) {
                    files
                } else {
                    manager.search(rootUri!!, query)
                }
                if (visible.isEmpty()) Text("Tidak ada file", modifier = Modifier.padding(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible, key = { it.uri.toString() }) { item ->
                        FileRow(
                            item = item,
                            onOpen = {
                                if (item.isDirectory) {
                                    backStack = backStack + listOf(currentUri!!)
                                    refresh(item.uri)
                                } else if (WorkspaceDocumentTools.isEditableText(item.name)) {
                                    val text = manager.readText(item.uri)
                                    if (text != null) {
                                        editorUri = item.uri
                                        editorName = item.name
                                        editorText = text
                                    } else notice = "File tidak bisa dibaca"
                                } else if (WorkspaceDocumentTools.isPdf(item.name)) {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(item.uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    runCatching { context.startActivity(intent) }.onFailure { notice = "Tidak ada aplikasi PDF" }
                                } else notice = "Format belum didukung editor WAW"
                            },
                            onMenu = { menuUri = item.uri }
                        )
                        if (menuUri == item.uri) {
                            FileMenu(
                                item = item,
                                onDismiss = { menuUri = null },
                                onRename = { menuUri = null; dialog = FileDialog.Rename(item.uri, item.name) },
                                onDelete = { menuUri = null; dialog = FileDialog.Delete(item.uri, item.name) },
                                onCopy = { menuUri = null; pendingTransfer = Transfer(item.uri, false); transferPicker.launch(null) },
                                onMove = { menuUri = null; pendingTransfer = Transfer(item.uri, true); transferPicker.launch(null) },
                                onShare = {
                                    menuUri = null
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = context.contentResolver.getType(item.uri) ?: "*/*"
                                        putExtra(Intent.EXTRA_STREAM, item.uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    runCatching { context.startActivity(Intent.createChooser(intent, "Bagikan file")) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    dialog?.let { currentDialog ->
        when (currentDialog) {
            FileDialog.CreateMenu -> AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text("Tambah") },
                text = { Text("Buat folder atau dokumen teks.") },
                confirmButton = {
                    Row {
                        TextButton(onClick = { dialog = FileDialog.CreateFolder }) { Text("Folder") }
                        TextButton(onClick = { dialog = FileDialog.CreateText }) { Text("Dokumen") }
                    }
                },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text("Batal") } }
            )
            FileDialog.CreateFolder -> NameDialog("Folder baru", "Nama folder", onCancel = { dialog = null }) { name ->
                val ok = currentUri?.let { manager.createFolder(it, name) } != null
                notice = if (ok) "Folder dibuat" else "Gagal membuat folder"
                dialog = null
                currentUri?.let(::refresh)
            }
            FileDialog.CreateText -> NameDialog("Dokumen baru", "Nama file, contoh: catatan.md", onCancel = { dialog = null }) { raw ->
                val name = if (raw.contains('.')) raw else "$raw.txt"
                val uri = currentUri?.let { manager.createFile(it, "text/plain", name) }
                if (uri != null) {
                    editorUri = uri; editorName = name; editorText = ""
                } else notice = "Gagal membuat dokumen"
                dialog = null
                currentUri?.let(::refresh)
            }
            is FileDialog.Rename -> NameDialog("Ganti nama", "Nama baru", currentDialog.name, onCancel = { dialog = null }) { name ->
                notice = if (manager.rename(currentDialog.uri, name) != null) "Nama diubah" else "Gagal mengganti nama"
                dialog = null; currentUri?.let(::refresh)
            }
            is FileDialog.Delete -> AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text("Hapus ${currentDialog.name}?") },
                text = { Text("Tindakan ini dapat menghapus file/folder dari lokasi yang dipilih.") },
                confirmButton = { TextButton(onClick = {
                    notice = if (manager.delete(currentDialog.uri)) "Dihapus" else "Gagal menghapus"
                    dialog = null; currentUri?.let(::refresh)
                }) { Text("Hapus") } },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text("Batal") } }
            )
        }
    }

    notice?.let { message ->
        AlertDialog(onDismissRequest = { notice = null }, title = { Text("WAW") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { notice = null }) { Text("OK") } })
    }

    if (attendanceDialog) {
        AttendanceDialog(
            records = attendanceManager.recent(),
            onDismiss = { attendanceDialog = false },
            onRecord = { type ->
                val activity = context as? androidx.fragment.app.FragmentActivity
                if (activity == null) {
                    notice = "Absensi hanya tersedia pada Activity Android."
                } else {
                    com.waw.messenger.security.BiometricGate(context).authenticate(activity, onResult = { success ->
                        notice = if (success) "Absensi $type tercatat pada ${attendanceManager.record(type)}" else "Verifikasi fingerprint dibatalkan atau gagal"
                    })
                }
                attendanceDialog = false
            }
        )
    }

    if (notesDialog) {
        AlertDialog(
            onDismissRequest = { notesDialog = false },
            title = { Text("Notes & Tasks") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan atau tugas baru") }
                    )
                    Button(onClick = {
                        notesStore.add(noteDraft)
                        noteDraft = ""
                        noteItems = notesStore.list()
                    }, modifier = Modifier.padding(top = 8.dp)) { Text("Simpan") }
                    if (noteItems.isNotEmpty()) {
                        Text("Catatan lokal", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
                        noteItems.take(8).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { notesDialog = false }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun AttendanceDialog(records: List<String>, onDismiss: () -> Unit, onRecord: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fingerprint Attendance") },
        text = {
            Column {
                Text("Fingerprint hanya digunakan untuk mencatat absensi. Data sidik jari tidak disimpan oleh WAW.")
                TextButton(onClick = { onRecord("MASUK") }) { Text("Catat Masuk") }
                TextButton(onClick = { onRecord("PULANG") }) { Text("Catat Pulang") }
                if (records.isNotEmpty()) {
                    Text("Riwayat lokal", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                    records.take(5).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}

@Composable
private fun FileRow(item: WorkspaceFileItem, onOpen: () -> Unit, onMenu: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(14.dp)) {
            Icon(if (item.isDirectory) Icons.Default.Folder else Icons.Default.Description, contentDescription = null)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(if (item.isDirectory) "Folder" else formatBytes(item.sizeBytes), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onMenu) { Icon(Icons.Default.MoreVert, "Menu") }
        }
    }
}

@Composable
private fun FileMenu(item: WorkspaceFileItem, onDismiss: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, onCopy: () -> Unit, onMove: () -> Unit, onShare: () -> Unit) {
    DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text("Rename") }, onClick = onRename)
        DropdownMenuItem(text = { Text("Copy") }, onClick = onCopy)
        DropdownMenuItem(text = { Text("Move") }, onClick = onMove)
        DropdownMenuItem(text = { Text("Share") }, onClick = onShare)
        DropdownMenuItem(text = { Text("Delete") }, onClick = onDelete)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentEditor(name: String, text: String, onTextChange: (String) -> Unit, onBack: () -> Unit, onSave: () -> Unit, onExportPdf: () -> Unit, modifier: Modifier) {
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        TopAppBar(title = { Text(name) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali") } }, actions = {
            IconButton(onClick = onExportPdf) { Icon(Icons.Default.PictureAsPdf, "Export PDF") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(value = text, onValueChange = onTextChange, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Isi dokumen") })
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Simpan") }
                OutlinedButton(onClick = onExportPdf, modifier = Modifier.weight(1f)) { Text("PDF") }
            }
        }
    }
}

@Composable
private fun NameDialog(title: String, label: String, initial: String = "", onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onCancel, title = { Text(title) }, text = { OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(label) }, singleLine = true) }, confirmButton = { TextButton(enabled = value.trim().isNotEmpty(), onClick = { onConfirm(value.trim()) }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } })
}

private sealed interface FileDialog {
    data object CreateMenu : FileDialog
    data object CreateFolder : FileDialog
    data object CreateText : FileDialog
    data class Rename(val uri: Uri, val name: String) : FileDialog
    data class Delete(val uri: Uri, val name: String) : FileDialog
}

private data class Transfer(val uri: Uri, val move: Boolean)

private fun formatBytes(size: Long?): String {
    if (size == null || size < 0) return "Ukuran tidak diketahui"
    if (size < 1024) return "$size B"
    if (size < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", size / 1024.0)
    if (size < 1024L * 1024L * 1024L) return String.format(Locale.ROOT, "%.1f MB", size / (1024.0 * 1024.0))
    return String.format(Locale.ROOT, "%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
}
