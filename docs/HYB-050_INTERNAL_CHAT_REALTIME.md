# HYB-050 — WAW Internal Chat, Status, dan Realtime

## Boundary

HYB-050 hanya menangani `waw_internal`. Pesan WhatsApp Business tetap melewati adapter dan webhook Meta, sedangkan WhatsApp personal WebView tidak dipetakan menjadi data internal WAW.

## Model

Pesan internal wajib membawa tenant, workspace, conversation, sender, kind, timestamps, dan referensi media bila ada. Presence, typing, delivery/read receipt, dan unread counter harus dibatasi oleh tenant/workspace membership. Status WAW memiliki visibility, expiry, optional media, dan deletion state.

## Realtime

Realtime service akan mengirim event versioned melalui WebSocket setelah authorization check. Event typing harus ephemeral dan tidak disimpan sebagai message. Presence memiliki TTL dan boleh berubah menjadi offline saat heartbeat habis. Delivery/read receipt hanya boleh dibuat oleh participant yang sah pada conversation.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Chat | Dua user dalam tenant/workspace yang sama dapat membuat dan membaca pesan |
| Isolation | User tenant lain tidak dapat membaca thread atau presence |
| Typing | Typing tidak disimpan sebagai pesan permanen |
| Receipts | Delivery/read hanya untuk participant yang berwenang |
| Status | Visibility, expiry, media reference, dan deletion diuji |
| Realtime | Reconnect tidak menggandakan event dan trace ID dipertahankan |
| Privacy | Channel badge internal tidak disamakan dengan WhatsApp Business |
