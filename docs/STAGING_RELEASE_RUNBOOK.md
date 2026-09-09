# WAW Hybrid Staging and Release Runbook

## Required secrets

Set these only in the deployment secret manager or GitHub Environment `production`:

```text
POSTGRES_PASSWORD
META_ACCESS_TOKEN
META_APP_SECRET
META_VERIFY_TOKEN
META_PHONE_NUMBER_ID
META_GRAPH_API_VERSION
PUBLIC_DOMAIN
TURN_URL
TURN_USERNAME
TURN_CREDENTIAL
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Never commit values, print them in logs, or embed them in APK assets.

## Staging database

Start `infra/staging/docker-compose.yml` on a host with Docker and a persistent volume. The PostgreSQL container applies the HYB-030 migration on first initialization. Run a backup before every migration and verify `/readyz` before enabling traffic.

## Meta Cloud API

Configure the HTTPS API URL as the Meta webhook callback. Verify the challenge with `META_VERIFY_TOKEN`, then send a message using the official test number. Confirm signature verification, idempotency, delivery status, and that the access token is absent from logs and client artifacts.

## Object storage and scanner

Deploy the media service with private object storage, short-lived signed URLs, and ClamAV (or an approved equivalent). Files remain quarantined until a clean scan. Scanner unavailability is fail-closed in production.

## TURN and relay

Provision a TURN service with short-lived credentials and set `TURN_URL`, `TURN_USERNAME`, and `TURN_CREDENTIAL` server-side. Publish the relay through `PUBLIC_DOMAIN` and test WSS from two networks. Do not place Cloudflare or TURN permanent secrets in APK configuration.

## Android physical QA

Install the signed APK and User Remote APK on two physical devices. Test system permission denial, MediaProjection, Accessibility, tap/swipe, keyboard, approved Back/Home/Recents, file transfer, foreground notification, reconnect, emergency revoke, and uninstall cleanup.

## Signed release QA

The GitHub production workflow must have all four Android signing secrets. Verify `apksigner`, SHA-256 checksum, install, update, and rollback. Attach test evidence to the release record. A green CI build without physical-device evidence is not a production approval.
