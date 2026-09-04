# WAW Backend

## Urutan implementasi

1. Database migration
2. Registration
3. Login/session
4. Authenticated API
5. User profile
6. Contacts
7. Conversation creation
8. Persistent messages
9. WebSocket authentication
10. Delivery/read receipts
11. Reconnect and sync

## Database

- `migrations/0001_auth_chat.sql` defines users, sessions, conversations, members, messages, and receipts.
- `migrations/0002_auth_security.sql` adds password salt, user status, and session revocation.
- Apply both migrations through the Cloudflare D1 migration workflow before production traffic.

## Authentication API

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me` with `Authorization: Bearer <token>`
- `POST /auth/logout` with `Authorization: Bearer <token>`

Passwords are derived with Web Crypto PBKDF2 using a per-user random salt. Session tokens are random and only their SHA-256 hashes are persisted.

## Status

- [x] Initial schema written
- [x] Security migration written
- [x] Registration API
- [x] Login API
- [x] Session validation
- [x] Logout/revocation
- [x] Authenticated WebSocket handshake
- [x] Conversation membership check for WebSocket messages
- [x] Persistent WebSocket message insert
- [ ] Migration applied to development D1
- [ ] Android authentication integration test
- [ ] Two-account integration test
- [ ] Delivery/read ACK
- [ ] Reconnect and sync

Do not mark the Chat phase complete until the two-account integration test passes.
