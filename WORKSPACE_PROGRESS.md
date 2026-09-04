# WAW Workspace — Progress & Implementation Plan

## Prinsip
Setiap fitur mengikuti `DESIGN → IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK → NEXT`.

## Status Saat Ini
- **W0 Foundation:** LOCKED (CI build + unit tests sebelumnya PASS).
- **W1 File Manager:** 🟡 IMPLEMENTED — PENDING VALIDATION.
- **W2 Document + PDF Core:** 🟡 IMPLEMENTED — PENDING VALIDATION.

## W1 — File Manager
Implemented:
- [x] Android Storage Access Framework / user-selected folder.
- [x] Persistable URI permission.
- [x] Browse folders/files.
- [x] Enter subfolders + back navigation.
- [x] Search/filter current folder.
- [x] Create folder.
- [x] Create TXT/Markdown-compatible text document.
- [x] Rename.
- [x] Copy.
- [x] Move (copy-then-delete compatibility path).
- [x] Delete.
- [x] Share file.
- [x] Basic file type/size display.
- [ ] Favorites.
- [ ] Recent-file index.
- [ ] Recursive/global storage usage.
- [ ] Real-device validation.
- [ ] PASS.
- [ ] LOCK.

## W2 — Document + PDF Core
Implemented:
- [x] TXT editor.
- [x] Markdown editor (plain-text editing with .md extension).
- [x] Save back to the selected SAF document.
- [x] Open PDF using installed Android PDF viewer.
- [x] Export current text/Markdown document to PDF.
- [x] Basic PDF generation using Android PdfDocument.
- [ ] PDF annotation.
- [ ] Merge/split/rotate/reorder/extract/compress utilities.
- [ ] Recent documents.
- [ ] Real-device validation.
- [ ] PASS.
- [ ] LOCK.

## Security / Storage Boundary
Workspace accesses only locations explicitly authorized by the user through Android SAF. WAW does not request broad storage access for these features. Android documents that `ACTION_OPEN_DOCUMENT_TREE` grants access to the selected directory subtree and that persistable URI permissions can preserve access across restarts. Some protected locations remain unavailable on Android 11+. 

No covert remote access, credential harvesting, raw fingerprint storage, or sensitive logging is part of W1/W2.

## Definition of Done
A stage is `LOCKED` only after implementation, successful build, real-device workflow validation, failure-case testing, security checks, documentation, and no known blockers.

## Next Gate
Run CI on the W1/W2 commits, install the generated APK on a real Android device/emulator, and execute the W1/W2 smoke checklist. Only after those pass may W1 and W2 be LOCKED.
