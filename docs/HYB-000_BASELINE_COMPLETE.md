# HYB-000 — Baseline Complete

## Scope

HYB-000 menetapkan baseline repository terbaru, CI, secret scan, review ownership, production release trigger, dan security hygiene sebelum masuk ke contracts.

## Completed

- Branch kerja berbasis `origin/main` terbaru.
- CODEOWNERS ditambahkan untuk `.github`, `app`, dan `user-remote`.
- `SECURITY.md` menetapkan disclosure dan secret boundary.
- Workflow `baseline-security.yml` menambahkan Gitleaks dan pemeriksaan credential WAW.
- Release workflow diperbaiki agar tidak menerbitkan release pada setiap push biasa.
- `.gitignore` dan artifact policy direview.

## Production Rule

Release production hanya berjalan melalui tag release atau `workflow_dispatch`, bukan setiap push ke `main`. Signing tetap wajib menggunakan `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, dan `ANDROID_KEY_PASSWORD` dari environment production.

## Acceptance Evidence

- Local Android build/test dari baseline terbaru harus lulus.
- Secret scan workflow harus lulus.
- Credential copy checker harus lulus.
- Pull request wajib mendapat review CODEOWNERS.
- Branch protection `main` harus aktif dengan required CI checks sebelum merge production.

## Remaining Repository Setting

Branch protection adalah pengaturan GitHub repository dan harus diverifikasi setelah workflow pertama selesai; setting tidak disimpan sebagai file source. Setelah protection aktif, HYB-000 dapat ditutup dan HYB-010 baru boleh dimulai.
