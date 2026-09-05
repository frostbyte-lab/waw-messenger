# Temuan dokumentasi resmi Meta — 5 September 2026

1. **WhatsApp Business Platform Cloud API** ditujukan untuk bisnis. Dokumentasi resmi menyebut Cloud API menyediakan messaging, rich media, interactive messages, calling, dan groups untuk WhatsApp Business Account. Transport utamanya adalah Graph API/HTTPS untuk pengiriman dan webhooks untuk event masuk/status.
   Sumber: https://developers.facebook.com/documentation/business-messaging/whatsapp/about-the-platform

2. **Get Started** mewajibkan developer registration, Meta app dengan use case WhatsApp, Business Portfolio, WhatsApp Business Account, business phone number, access token, dan webhook. Dokumentasi juga membedakan integrasi ini dari pengguna akun personal biasa.
   Sumber: https://developers.facebook.com/documentation/business-messaging/whatsapp/get-started

3. **Webhooks resmi** mengirim incoming messages, outgoing delivery/read status, call events, account events, serta field seperti `history`, `smb_app_state_sync`, dan `smb_message_echoes` untuk konteks business customer yang onboarded melalui solution provider. Webhook dapat di-retry hingga 7 hari dan payload dapat terduplikasi, sehingga implementasi harus idempotent.
   Sumber: https://developers.facebook.com/documentation/business-messaging/whatsapp/webhooks/overview

4. **Calling API** berlaku untuk business number yang memakai Cloud API, bukan WhatsApp Business app. Calling membutuhkan permission, subscription ke `calls` webhook, enablement pada business phone number, dan memakai Graph API/webhook signaling dengan WebRTC atau SIP. Calling API bukan API umum untuk mengendalikan panggilan akun WhatsApp personal.
   Sumber: https://developers.facebook.com/documentation/business-messaging/whatsapp/calling

5. **Implikasi untuk WAW pilihan B:** WAW dapat dibangun sebagai dashboard/client pendamping resmi untuk WhatsApp Business Platform/Cloud API atau solution-provider onboarding yang disetujui, tetapi tidak boleh mengakses akun WhatsApp personal melalui protokol privat, menyalin session, membaca database lokal WhatsApp, atau menyamar sebagai linked device tanpa jalur resmi.
