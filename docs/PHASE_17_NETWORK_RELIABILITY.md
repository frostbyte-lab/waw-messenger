# WAW Phase 17 — Network Reliability

Status: IMPLEMENTED — BUILD/DEVICE TEST PENDING

## Scope
- Detect validated internet connectivity with `ConnectivityManager.NetworkCallback`.
- Expose online/offline state through `StateFlow`.
- Avoid treating a connected Wi-Fi/mobile transport as guaranteed internet access.
- Provide bounded exponential retry for transient WAW-owned operations.
- Reconnect the legacy WebSocket repository after unexpected close/failure.
- Provide WorkManager infrastructure for persistent WAW-owned sync work when network constraints are met.
- Keep network logs free of credentials, session tokens, cookies, or private keys.

## Reliability rules
1. User-initiated network work should check connectivity before attempting expensive operations.
2. Retry only transient failures; do not blindly retry authentication/authorization failures.
3. Exponential backoff must be bounded.
4. Persistent background work uses WorkManager with network constraints.
5. WebSocket close initiated by the app must not trigger reconnect.
6. This phase does not implement or emulate undocumented WhatsApp protocols.

## Test gate
Before LOCK:
- Gradle build succeeds.
- Unit tests cover retry limits/backoff behavior.
- Device/emulator verifies Wi-Fi → offline → online transitions.
- WebSocket reconnect is verified against a test server.
- WorkManager request waits for network constraint.

Phase 16 remains parked. Phase 17 is not LOCKED until the test gate passes.
