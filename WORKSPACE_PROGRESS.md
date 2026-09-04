# WAW Workspace — Progress & Implementation Plan

## Tujuan

WAW Workspace adalah modul produktivitas dan keamanan milik WAW sendiri. Workspace tidak mengambil alih otoritas akun WhatsApp dan tidak bergantung pada protokol privat WhatsApp untuk fitur-fitur WAW-owned.

## Prinsip Pengerjaan

Setiap fitur wajib mengikuti:

```text
DESIGN → IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK → NEXT
```

Tidak boleh mengerjakan fitur berikutnya jika fitur sebelumnya masih memiliki blocker.

## Modul Workspace

### A. Remote
- [ ] Remote PC
- [ ] Remote Android
- [ ] Device pairing
- [ ] Device authorization
- [ ] Screen viewing
- [ ] Input control: mouse / keyboard / touch
- [ ] File transfer
- [ ] Connection status
- [ ] Disconnect / revoke access

**Security:** remote control wajib memakai pairing/authorization eksplisit, secure transport, device identity, permission, dan revoke access. Tidak boleh ada remote access diam-diam.

### B. Document
- [ ] File browser
- [ ] TXT editor
- [ ] Markdown editor
- [ ] PDF viewer
- [ ] PDF annotation
- [ ] Save/export
- [ ] Recent documents
- [ ] Search

### C. Camera Scanner
- [ ] Camera permission
- [ ] Document capture
- [ ] Edge detection
- [ ] Auto crop
- [ ] Perspective correction
- [ ] Image enhancement
- [ ] Multi-page scan
- [ ] Reorder pages
- [ ] Image → PDF
- [ ] PDF preview
- [ ] Export/share

### D. Custom Watermark
- [ ] Text watermark
- [ ] Image/logo watermark
- [ ] Position
- [ ] Size
- [ ] Opacity
- [ ] Rotation
- [ ] Color
- [ ] Repeat/tile
- [ ] Per-page configuration
- [ ] Watermark presets
- [ ] Apply to exported PDF/images

### E. Biometric / Fingerprint Security
- [ ] Workspace lock
- [ ] Secure Vault lock
- [ ] Document lock
- [ ] BiometricPrompt integration
- [ ] Device credential fallback where appropriate
- [ ] Auto-lock timeout
- [ ] Lock after background
- [ ] Failed-auth handling

Use Android's system biometric authentication rather than implementing or storing fingerprint data inside WAW.

### F. File Manager
- [ ] Folders
- [ ] Rename
- [ ] Copy
- [ ] Move
- [ ] Delete
- [ ] Share
- [ ] Sort/filter
- [ ] Favorites
- [ ] Recent files
- [ ] Storage usage

### G. PDF Tools
- [ ] Merge PDF
- [ ] Split PDF
- [ ] Rotate pages
- [ ] Reorder pages
- [ ] Extract pages
- [ ] Compress PDF
- [ ] Image → PDF
- [ ] PDF → image

### H. Notes & Tasks
- [ ] Notes
- [ ] Checklist
- [ ] Tasks
- [ ] Attach files
- [ ] Search
- [ ] Local persistence

### I. Network / IP Information

This feature is for network diagnostics and IP information, not covert tracking of people.

- [ ] Public IP
- [ ] Local IP
- [ ] IPv4 / IPv6
- [ ] ISP / ASN where available
- [ ] Country
- [ ] Region
- [ ] City estimate where available
- [ ] Timezone
- [ ] Approximate map display
- [ ] DNS test
- [ ] Ping/latency
- [ ] Connection diagnostics
- [ ] Speed test

**Important:** IP geolocation is approximate and must never be presented as a person's exact GPS location. Exact device location requires the appropriate Android location permission and user consent.

### J. WAW Shield — Anti-Judol / Anti-Phishing

- [ ] Domain reputation check
- [ ] Suspicious-domain warning
- [ ] Gambling/judol blocklist
- [ ] Custom blocklist
- [ ] Phishing detection
- [ ] Suspicious redirect detection
- [ ] Safe browsing warning page
- [ ] Block / Allow decision
- [ ] Block history
- [ ] Report domain
- [ ] Local-first filtering where practical
- [ ] Privacy-preserving reputation lookup

The shield must warn/block based on transparent rules or reputable threat intelligence. It must not claim certainty when a domain cannot be classified reliably.

### K. Secure Vault
- [ ] Private files
- [ ] Biometric unlock
- [ ] Encrypted storage
- [ ] Auto-lock
- [ ] Secure deletion policy
- [ ] Hide/private metadata where practical

### L. Backup & Sync
- [ ] Workspace backup
- [ ] Restore
- [ ] Export/import
- [ ] Version history
- [ ] Conflict handling
- [ ] Optional WAW-owned sync

### M. Clipboard Manager
- [ ] Clipboard history
- [ ] Pin item
- [ ] Search
- [ ] Auto-expiration
- [ ] Sensitive-content exclusion

### N. Universal Search

Search across WAW-owned Workspace data:

- [ ] Documents
- [ ] PDFs
- [ ] Notes
- [ ] Tasks
- [ ] Files
- [ ] Workspace items
- [ ] Devices

## Recommended Implementation Order

### Stage W0 — Workspace Foundation
- [ ] Define Workspace navigation
- [ ] Define local data model
- [ ] Define permission model
- [ ] Define security boundaries
- [ ] Define Workspace storage structure
- [ ] Create feature flags/status model
- [ ] Build Workspace shell
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W1 — File Manager
- [ ] Browse
- [ ] Folder operations
- [ ] Rename/copy/move/delete
- [ ] Search/recent
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W2 — Document + PDF Core
- [ ] TXT/Markdown editor
- [ ] PDF viewer
- [ ] Basic annotation
- [ ] PDF utilities
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W3 — Camera Scanner
- [ ] Capture
- [ ] Crop/correction
- [ ] Enhancement
- [ ] Multi-page
- [ ] Image → PDF
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W4 — Custom Watermark
- [ ] Text/logo
- [ ] Position/style
- [ ] Presets
- [ ] Apply/export
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W5 — Fingerprint + Secure Vault
- [ ] Biometric lock
- [ ] Secure storage
- [ ] Vault
- [ ] Auto-lock
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W6 — Network / IP Diagnostics
- [ ] IP information
- [ ] Approximate geolocation
- [ ] DNS/ping
- [ ] Network diagnostics
- [ ] Privacy review
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W7 — WAW Shield
- [ ] Blocklist engine
- [ ] Domain classification
- [ ] Anti-judol warning
- [ ] Anti-phishing
- [ ] Redirect checks
- [ ] Privacy review
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W8 — Notes + Tasks
- [ ] Notes
- [ ] Checklist/tasks
- [ ] Attachments
- [ ] Search
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W9 — Backup / Sync
- [ ] Backup
- [ ] Restore
- [ ] Versioning
- [ ] Conflict handling
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W10 — Remote PC / Android
- [ ] Device pairing
- [ ] Authorization
- [ ] Secure transport
- [ ] Screen viewing
- [ ] Input control
- [ ] File transfer
- [ ] Revoke access
- [ ] TEST on owned devices
- [ ] Security review
- [ ] PASS
- [ ] LOCK

Remote control is intentionally later in the Workspace sequence because it has the highest security impact.

### Stage W11 — Universal Search
- [ ] Unified index
- [ ] Search ranking
- [ ] Result navigation
- [ ] Permission filtering
- [ ] TEST
- [ ] PASS
- [ ] LOCK

### Stage W12 — Workspace Final Integration
- [ ] Cross-feature navigation
- [ ] Permission audit
- [ ] Storage audit
- [ ] Performance test
- [ ] Offline behavior
- [ ] Error recovery
- [ ] Security regression
- [ ] Full Workspace test
- [ ] PASS
- [ ] LOCK

## Status

| Stage | Status |
|---|---|
| W0 Foundation | NOT STARTED |
| W1 File Manager | NOT STARTED |
| W2 Document + PDF | NOT STARTED |
| W3 Scanner | NOT STARTED |
| W4 Watermark | NOT STARTED |
| W5 Fingerprint + Vault | NOT STARTED |
| W6 Network/IP | NOT STARTED |
| W7 WAW Shield | NOT STARTED |
| W8 Notes + Tasks | NOT STARTED |
| W9 Backup/Sync | NOT STARTED |
| W10 Remote | NOT STARTED |
| W11 Universal Search | NOT STARTED |
| W12 Final Integration | NOT STARTED |

## Security Rules

- No covert remote access.
- No hidden persistence on another person's device.
- No credential theft.
- No password/OTP harvesting.
- No storing raw fingerprint data.
- No claiming IP geolocation is exact physical location.
- No collection of unnecessary personal data.
- No sensitive secrets in logs.
- User must explicitly authorize device pairing and remote control.
- Revoke/disconnect must be available.

## Definition of Done

A Workspace stage is `LOCKED` only when:

1. Implementation is complete.
2. Build succeeds.
3. Main workflow works on a real device.
4. Failure cases have been tested.
5. Security checks pass.
6. No sensitive data is exposed in logs.
7. Documentation/status is updated.
8. No known blocker remains.

## Relationship to ROADMAP.md

Workspace is a WAW-owned product module. It complements the WhatsApp linked/companion-device roadmap but does not replace or alter the official WhatsApp linking requirements.

The WhatsApp linked-device path remains governed by `ROADMAP.md`. Workspace features can be developed independently where they do not require undocumented WhatsApp capabilities.
