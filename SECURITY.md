# Security Policy

WAW Hybrid memisahkan `waw_internal` dan `whatsapp_business`, tidak menyimpan credential WhatsApp personal, dan tidak menaruh provider secret di client.

## Pelaporan

Jangan membuat issue publik untuk credential, token, exploit remote, atau data pribadi. Gunakan GitHub Security Advisories pada repository ini atau koordinasikan dengan maintainer repository.

## Baseline

Semua perubahan production harus melewati CI, secret scan, tenant/isolation review jika relevan, dan review owner. Credential Meta, Cloudflare, signing keystore, TURN, database, dan object storage hanya boleh berada di secret manager/GitHub Actions secrets.
