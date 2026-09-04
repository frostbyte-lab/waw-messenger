# WAW — Phase 18 Legacy Standalone Chat Audit

Status: IMPLEMENTED — legacy isolated from the production UI path; deletion deferred.

## Scope

The following components were audited:

- `app/src/main/java/com/waw/messenger/auth/AuthRepository.kt`
- `app/src/main/java/com/waw/messenger/chat/ChatRepository.kt`
- `app/src/main/java/com/waw/messenger/chat/WebSocketChatRepository.kt`
- `backend/worker.js`
- `backend/migrations/0001_auth_chat.sql`
- `backend/migrations/0002_auth_security.sql`
- `app/src/main/java/com/waw/messenger/MainActivity.kt`

## Findings

### AuthRepository

Legacy WAW-account authentication using username/email/password and a WAW server session token. This is not the identity model for the linked/companion-device target.

Decision: **LEGACY — DO NOT USE AS THE PRODUCTION IDENTITY PATH.**

### ChatRepository / LocalChatRepository

In-memory sample conversation/message implementation. It is useful as a UI development seam but is not a WhatsApp data source and must not be presented as synchronized WhatsApp chat state.

Decision: **LEGACY / UI TEST SEAM.**

### WebSocketChatRepository

Connects to the old WAW Worker chat service and contains reconnect behavior. It is a standalone WAW transport, not a WhatsApp linked-device transport.

Decision: **LEGACY — DO NOT USE FOR WHATSAPP TRANSPORT.**

### backend/worker.js + D1 chat schema

Implements WAW-owned users, sessions, conversations and messages. This architecture conflicts with the target rule that WhatsApp account/session/chat state must not be copied into a WAW database or substituted by a WAW backend.

Decision: **LEGACY BACKEND — PRESERVE FOR AUDIT/REFERENCE, DO NOT EXTEND AS WHATSAPP BACKEND.**

### MainActivity production path

The production UI no longer instantiates `LocalChatRepository`, `ChatRepository`, or `WebSocketChatRepository`. Instead, after local app unlock, it explicitly reports that the official linked/companion production path is not yet available.

Decision: **PRODUCTION/LEGACY BOUNDARY ISOLATED.**

## Production-path rule

Until an official linked/companion-device mechanism is actually available and validated for the target Android implementation:

- Do not claim WhatsApp account linking is implemented.
- Do not use WAW username/password as the WhatsApp identity.
- Do not use WAW D1 as the source of truth for WhatsApp chats.
- Do not extract, copy, replay or store WhatsApp private credentials, tokens, cookies or keys.
- Unsupported/private protocol operations remain `BLOCKED / NOT_SUPPORTED`.

## Deletion decision

No mass deletion was performed. Legacy code remains available until a validated replacement exists and a separate migration/deletion decision is made.

## Phase 18 acceptance

- [x] AuthRepository audited
- [x] ChatRepository audited
- [x] WebSocketChatRepository audited
- [x] D1 chat schema/backend audited
- [x] Legacy classification documented
- [x] Production/legacy boundary documented
- [x] MainActivity no longer uses legacy chat repositories
- [x] No mass deletion
- [ ] Full Android build validation
- [ ] Device/instrumentation validation

Phase 18 can only be **LOCKED** after the repository build/test gate is green and device/instrumentation validation is completed.
