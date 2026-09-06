# WAW Remote Components

## Relay

`relay/` is a minimal pairing relay. Run it behind a trusted TLS reverse proxy and expose only `wss://`; the relay forwards messages only after a six-digit host/viewer pairing handshake and expires sessions automatically.

## Distribusi APK

Link undangan hanya boleh mengarah ke APK **WAW utama** pada perangkat target. Tidak ada APK Companion terpisah: modul Remote User (`RemoteHostActivity`) sudah berada di dalam APK WAW utama. Setelah WAW terpasang, link `waw://remote/invite?relay=wss%3A%2F%2Frelay.example` membuka modul tersebut dan mengisi alamat relay; User tetap harus membuat OTP serta menyetujui izin screen capture secara manual. APK **Admin/Operator** bukan artefak undangan dan tidak boleh dibagikan ke lingkungan lain. Modul Admin dilindungi permission Android level `signature`, sehingga hanya dapat dibuka ketika WAW utama yang ditandatangani dengan sertifikat yang sama sudah terpasang. Build release juga hanya mengunggah artefak dari modul `app`; APK `admin-android` tidak dipublikasikan.

```bash
cd remote/relay
npm install
PORT=8787 npm start
```

## Windows peer

`windows-agent/` is a CLI peer useful for transport smoke tests and local frame capture. It does not silently control a host.

```powershell
cd remote/windows-agent
npm install
$env:WAW_RELAY_URL="wss://your-relay.example/"
$env:WAW_PAIRING_CODE="123456"
npm start
```

The Android host must approve the session. Input commands are sent only from explicit CLI commands. Production packaging still requires a signed Windows UI, secure credential storage, certificate pinning, and a user-facing audit log.

## Relay WSS production

`relay/Dockerfile`, `relay/docker-compose.yml`, dan `relay/Caddyfile.example` menyediakan deployment relay di balik TLS. Salin `Caddyfile.example` menjadi `Caddyfile`, ganti domain dengan hostname yang DNS-nya mengarah ke server, lalu jalankan `docker compose up -d --build`. APK hanya menerima URL `wss://`; jangan expose port relay mentah ke internet.

### Cloudflare Quick Tunnel tanpa domain sendiri

Untuk uji cepat, jalankan `chmod +x relay/start-quick-tunnel.sh && relay/start-quick-tunnel.sh` pada server yang memiliki Docker dan `cloudflared`. Cloudflare akan menampilkan URL acak `https://<random>.trycloudflare.com`; gunakan URL yang sama dengan skema `wss://` pada APK Admin dan APK User. Quick Tunnel tidak memerlukan akun/domain, tetapi URL berubah ketika proses tunnel berhenti atau restart, sehingga tidak cocok untuk produksi permanen.

## Remote capabilities

Transfer file Admin → User menggunakan picker Android, konfirmasi User, Storage Access Framework, checksum SHA-256, dan batas 5 MB per file. Text input menggunakan node input yang sedang fokus melalui Accessibility Service. Approved actions hanya mengizinkan `BACK`, `HOME`, `RECENTS`, dan `NOTIFICATION_SHADE`; shell arbitrer tidak didukung.
