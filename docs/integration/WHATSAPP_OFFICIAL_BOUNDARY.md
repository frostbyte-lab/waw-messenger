# WAW — WhatsApp Official Integration Boundary

## Purpose

Dokumen ini menetapkan batas integrasi WAW dengan WhatsApp agar implementasi tidak salah arah.

## Target

WAW menargetkan pengalaman **linked / companion device** untuk akun WhatsApp yang sah, tetapi WAW tidak boleh mengimplementasikan protokol privat WhatsApp melalui reverse engineering, replay credential, atau session extraction.

## Current official findings

WhatsApp Help Center saat ini mendokumentasikan linked devices dan mencantumkan **Companion Phones** sebagai jenis perangkat tertaut. WhatsApp juga mendokumentasikan linking companion phone menggunakan QR code melalui aplikasi WhatsApp yang didukung.

WhatsApp memperingatkan bahwa linking melalui aplikasi atau website tidak resmi dapat membahayakan akun dan dapat menyebabkan pembatasan atau pemblokiran.

Dokumentasi resmi yang digunakan sebagai source of truth:

- About linked devices
- How to link a device
- How to link a device using phone number
- Companion phone documentation

## Boundary

### Allowed

- Meniru pola UX linked-device dengan implementasi UI milik WAW.
- Menjalankan secure local storage.
- Menjalankan biometric/app lock.
- Membuka aplikasi/flow resmi yang memang disediakan WhatsApp.
- Menggunakan interface resmi/authorized bila suatu saat tersedia untuk WAW.
- Mengembangkan Workspace milik WAW.

### Not allowed

- Meminta password WhatsApp.
- Mengambil OTP untuk dijadikan credential WAW.
- Mengekstrak token/cookie/session secret WhatsApp.
- Mengambil private key WhatsApp.
- Replay session atau credential.
- Memalsukan perangkat atau identitas linked device.
- Menebak private endpoint/protocol lalu menggunakannya sebagai API.
- Menonaktifkan atau melemahkan security control.
- Menyalin database internal WhatsApp.

## Consequence for Track A

Fitur berikut hanya boleh diberi status **DONE/LOCKED** apabila tersedia interface resmi/authorized yang dapat dipakai aplikasi WAW:

- Link Device
- Identity sync
- Contacts
- Chat
- Message sync
- Media
- Voice notes
- Groups
- Notifications tied to WhatsApp events
- Calls
- Status/Updates
- Linked-device management

Jika interface tersebut tidak tersedia, status wajib tetap:

`BLOCKED / NOT_SUPPORTED`

## WAW-owned work that can proceed independently

- Android production foundation
- Local security
- Biometric lock
- Workspace
- File manager
- Document/PDF tools
- Scanner
- Watermark
- Network/IP diagnostics
- WAW Shield
- Notes/tasks
- Backup/sync for WAW-owned data
- Remote access between explicitly authorized devices

## Test rule

Tidak ada `LOCK` hanya berdasarkan kemiripan UI. Test harus membuktikan perilaku nyata pada perangkat milik penguji dan mekanisme yang sah.
