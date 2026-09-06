# WAW Remote Components

## Relay

`relay/` is a minimal pairing relay. Run it behind a trusted TLS reverse proxy and expose only `wss://`; the relay forwards messages only after a six-digit host/viewer pairing handshake and expires sessions automatically.

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
