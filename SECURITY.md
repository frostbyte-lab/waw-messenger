# Security Policy — WAW Hybrid

## Prinsip

WAW memisahkan channel `waw_internal` dan `whatsapp_business`. Setiap request wajib melewati autentikasi, tenant guard, role guard, channel boundary, dan audit policy yang sesuai.

## Credential dan secret

Secret Meta, database, queue, object storage, TURN, signing key, dan webhook verification token hanya boleh berada di secret manager atau GitHub Actions secrets. Secret dilarang masuk ke repository, frontend bundle, Cloudflare Pages, APK, localStorage, log, screenshot, atau artifact.

`.env.example` hanya berisi nama variable tanpa nilai sensitif. Gunakan secret manager pada deployment production.

## Batas WhatsApp

WAW hanya menggunakan WhatsApp Web resmi untuk linking personal dan WhatsApp Business Cloud API resmi Meta untuk komunikasi bisnis. WAW tidak meminta password WhatsApp, tidak menyimpan cookie/QR/session token, dan tidak mengirim isi chat personal ke backend WAW.

## Remote Workspace

Remote Workspace memerlukan consent eksplisit, token one-time dengan expiry, room isolation, foreground notification, MediaProjection/Accessibility consent, revoke, emergency disconnect, dan audit lifecycle. Cloudflare Tunnel hanya transport; bukan mekanisme permission Android.

## Pelaporan

Jangan membuka credential, token, isi pesan, atau payload sensitif dalam issue publik. Laporkan potensi masalah keamanan melalui maintainer repository dengan detail reproduksi minimum dan tanpa secret.

## Required checks

Pull Request ke `main` wajib lulus:

- `build`
- `secret-scan`

Perubahan production harus memiliki test evidence, security notes, dan rollback/cleanup plan.
