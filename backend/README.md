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

Migration `migrations/0001_auth_chat.sql` defines the initial users, sessions, conversations, members, messages, and message receipts tables.

Apply migrations through the Cloudflare D1 migration workflow before connecting production traffic.

## Status

- [x] Initial schema written
- [ ] Migration applied to development D1
- [ ] Registration API
- [ ] Login API
- [ ] Session validation
- [ ] Authenticated chat API
- [ ] Persistent WebSocket chat
- [ ] Delivery/read ACK
- [ ] Two-account integration test

Do not mark the Chat phase complete until the integration test with two authenticated accounts passes.
