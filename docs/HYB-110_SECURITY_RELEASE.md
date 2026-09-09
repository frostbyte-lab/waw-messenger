# HYB-110 — Security, Observability, dan Release

## Threat model

| Asset | Threat | Control | Evidence |
|---|---|---|---|
| Tenant data | Cross-tenant access | Tenant/workspace guard on every resource and event | Isolation tests |
| Meta credentials | Client/log disclosure | Server-side secret storage and redaction | Secret scan and log review |
| Remote session | Replay or unauthorized control | One-time invite, expiry, nonce, consent, revoke | Session lifecycle tests |
| Media | Malware or data leakage | Quarantine, scan, signed URL, retention | Media pipeline tests |
| Webhook | Forgery or duplicate processing | Signature verification and idempotency | Webhook tests |
| Realtime | Token/session abuse | Membership authorization, short-lived credentials, reconnect limits | Gateway tests |
| Backup | Unauthorized restore or deletion | Role guard, audit, restore drill | Recovery evidence |

## Observability

Logs must be structured and must not include Meta tokens, Cloudflare tokens, passwords, QR payloads, cookies, raw message text, media bytes, or screen frames. Every cross-service event carries a trace ID, tenant ID, workspace ID, operation name, result, and latency. Sensitive identifiers are redacted or hashed according to the retention policy.

Required metrics include API latency, error rate, webhook lag, retry count, dead-letter depth, media scan latency, signed URL failures, WebRTC connection success, remote session revoke latency, and foreground service failure count.

## Verification plan

| Check | Required evidence |
|---|---|
| Dependency scan | Reproducible report with reviewed high/critical findings |
| Secret scan | CI pass and repository history review |
| SAST/DAST | Findings triaged and blocking findings closed |
| Rate limit | Auth, webhook, upload, signaling, and remote-control abuse tests |
| Tenant isolation | Positive and negative authorization matrix |
| Backup restore | Restore into isolated staging and verify checksums |
| Android QA | Install, update, permission denial, revoke, foreground notification, reconnect |
| Web QA | Desktop/mobile shell, offline/retry, consent, empty/error states |

## Release gate

A release requires successful CI, security review, staging smoke test, manual Android and browser matrix, documented rollback commit, migration backup, incident contact, and release notes. Any unresolved critical security issue blocks release. Rollback must disable new capabilities, preserve audit records, and avoid restoring revoked remote sessions.

## Acceptance criteria

HYB-110 is complete when the threat model, security scan evidence, observability dashboard definitions, backup restore drill, tenant-isolation tests, manual QA matrix, rollback plan, and release checklist are reviewed and attached to the release record.
