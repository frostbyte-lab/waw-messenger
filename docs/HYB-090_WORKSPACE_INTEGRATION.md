# HYB-090 — Workspace Integration

## Scope

Workspace menyatukan tools WAW seperti file manager, documents/PDF, scanner, watermark, vault, diagnostics, shield, notes, backup, dan search di dalam shell Hybrid. Setiap resource memiliki tenant dan workspace owner. Setiap operasi membutuhkan membership dan role yang sesuai.

## Media boundary

File dan hasil scanner memakai media service HYB-070. Workspace tidak menulis object langsung ke storage dan tidak menggunakan endpoint chat untuk upload. Resource hanya menyimpan `mediaId` serta metadata. Resource sensitif atau restricted memerlukan policy tambahan dan audit.

## Authorization

Search dibatasi pada resource tenant/workspace yang dapat diakses actor. Share, export, restore, delete, shield, dan vault operation wajib melalui authorization check dan audit. Backup restore tidak boleh menghapus data aktif tanpa operation guard dan recovery record.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Shell | Seluruh tools tampil dalam shell WAW Hybrid dengan channel/boundary yang jelas |
| Isolation | Tenant lain tidak dapat melihat resource, search result, atau metadata |
| Media | Upload/download melewati media service dan state scan dihormati |
| Sensitive | Vault, shield, export, dan restore memerlukan permission serta audit |
| Sharing | Share memiliki target user/workspace dan tidak melampaui tenant |
| Search | Search tidak mengembalikan data yang tidak berwenang |
| Recovery | Delete/restore meninggalkan trace dan recovery state |
| Smoke test | Setiap tool memiliki success, empty, error, offline, dan retry state |
