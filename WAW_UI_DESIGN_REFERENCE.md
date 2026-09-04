# WAW UI — Design Reference & Implementation Reminder

> **STATUS: DESIGN LOCK / IMPLEMENTATION REFERENCE**  
> This document is the visual source of truth for the WAW application UI. Do not replace the design with a generic Material3 screen unless required to fix a real bug.

## Reference image

The visual reference supplied in chat is **1000534850.png** (864 × 1536). It contains the intended WAW home screen, chat screen, Workspace view, and animation specifications.

**Important:** the uploaded image is a chat attachment and is not automatically a repository asset. Keep this MD as the permanent implementation reminder. If the exact image is later added to the repository, embed it here as:

```md
![WAW UI Design Reference](docs/design/waw-ui-reference.png)
```

## Global visual rules

- All primary app backgrounds: **WHITE**.
- WAW branding: use the supplied **3D green/blue WAW logo** (green chat bubble + blue W bubble with W), not the old purple shield logo.
- Visual direction: polished WhatsApp / WhatsApp Business / Meta-inspired usability, but branded as WAW and not a copy of their proprietary assets.
- Main accent: WAW green/teal.
- Secondary accents may be used for Workspace cards: blue, green, purple, orange.
- Cards: clean white surfaces, subtle borders/shadows, rounded corners.
- Typography: clear, compact, modern, high readability.
- Avoid dark/purple backgrounds for the normal WAW experience.
- Preserve safe-area/status-bar readability.
- UI must remain usable on small Android screens.

## Main navigation

Top horizontal navigation must support smooth horizontal scrolling until **Workspace** is reached.

Tabs shown in the reference:

1. Chat
2. Panggilan
3. Status
4. Fitur
5. Workspace

Requirements:

- Horizontal swipe/drag.
- Smooth inertial scrolling.
- Active-tab animation.
- Workspace must be reachable by scrolling, not hidden behind a static layout.
- Selecting a tab should transition smoothly rather than abruptly replacing the whole screen.
- Bottom navigation mirrors the main destinations where appropriate.

## Home / Chat list

Header:

- WAW logo.
- Title: **WAW**.
- Small BUSINESS badge.
- Subtitle/brand text: **WhatsApp Workspace** where applicable.
- Search action.
- More/menu action.

Quick Access section:

- Section title: **Workspace Quick Access**.
- Action: **Lihat semua**.
- Four cards:
  - Dokumen — file count.
  - Tugas — active task count.
  - Kalender — meeting count.
  - File — storage size.

Conversation list:

- Section title: **OBROLAN**.
- Unread/new count badge.
- Avatar with initials or WAW logo.
- Conversation name.
- Optional project/client/team label.
- Last message preview.
- Typing preview when active.
- Time.
- Unread badge.
- Online/activity indication when relevant.

WAW Insight:

- Include a compact insight/activity surface without overwhelming the chat list.

## Chat screen

Header:

- Back button.
- WAW/contact avatar.
- Contact name.
- Green online / typing state.
- Call action.
- More action.

Message surface:

- Light, clean chat background.
- Incoming messages: white/light bubbles.
- Outgoing messages: WAW green/teal bubbles.
- Rounded bubbles with subtle elevation/border.
- Timestamp on each message.
- Sent/delivered/read status icons.
- Optional reply/reaction metadata.

Composer:

- Plus/attachment button.
- Text field: **Ketik pesan...**.
- Emoji action.
- Microphone when empty.
- Send action when text exists.
- Composer should rise naturally with the keyboard.

## Required animations

These are part of the UI specification, not optional decoration.

### 1. Smooth top navigation scroll

- Horizontal scroll with natural inertia.
- Active tab indicator transitions smoothly.
- Workspace tab remains easy to discover.
- Avoid jitter and abrupt snapping.

### 2. Incoming message animation

- New incoming message enters from the left.
- Small scale/fade/glow combination.
- Animation must settle quickly and not distract from reading.

### 3. Outgoing message animation

- New outgoing message enters from the right.
- Subtle upward/scale/fade motion.
- Bubble should feel physically connected to the composer.

### 4. Message status transition

Support visual states:

`Mengirim → Terkirim → Tersampaikan → Dibaca`

- Transition icons smoothly.
- Read state uses the WAW blue/green visual treatment defined by the design.
- Failed state must be visibly different and retryable.

### 5. Typing indicator

- Text: `sedang mengetik...`
- Three animated dots.
- Continuous but lightweight animation.
- Stop immediately when typing state ends.

### 6. New-message badge

- Pop/bounce animation.
- Badge must not cause layout jumps.

### 7. Attachment upload

- File card with filename/type/size.
- Progress indicator.
- Smooth progress animation.
- Cancel/retry path where supported.

### 8. Composer + keyboard

- Composer follows IME/keyboard movement smoothly.
- No large jumps when keyboard opens/closes.
- Send button can transition between microphone and send states.

### 9. Voice message

- Animated waveform.
- Play/pause state.
- Duration.
- Progress animation while playing.

### 10. Chat opening / closing

- Conversation list item transitions naturally into the chat screen.
- Back navigation should feel continuous.
- Do not use a harsh full-screen replacement animation.

### 11. Smooth conversation list

- Lazy list scrolling with natural inertia.
- New items may animate into place without shifting unrelated rows excessively.
- Preserve scroll position when returning from a chat.

## Workspace view

Workspace is a first-class WAW destination, not an external utility page.

Workspace quick-access cards:

- Dokumen
- Tugas
- Kalender
- File

Workspace activity panel:

- Recent file download/activity.
- Meeting activity.
- Task completion/activity.
- `Lihat semua` action.

Existing Workspace functionality must remain intact:

- Android Storage Access Framework.
- User-selected folder access.
- Browse/search.
- Create/rename/copy/move/delete/share.
- TXT/Markdown editing.
- PDF opening/export.
- Workspace security/storage boundary.

Do not remove existing Workspace functionality while implementing the new visual shell.

## Screen hierarchy

```text
WAW
├── Home / Chat
│   ├── Header
│   ├── Horizontal destination tabs
│   │   ├── Chat
│   │   ├── Panggilan
│   │   ├── Status
│   │   ├── Fitur
│   │   └── Workspace
│   ├── Workspace Quick Access
│   │   ├── Dokumen
│   │   ├── Tugas
│   │   ├── Kalender
│   │   └── File
│   ├── Obrolan
│   └── WAW Insight
├── Chat Detail
│   ├── Header
│   ├── Message list
│   ├── Typing indicator
│   └── Composer
├── Panggilan
├── Status
├── Fitur
└── Workspace
    ├── File Manager
    ├── Documents
    └── PDF
```

## Implementation architecture reminder

Prefer reusable Compose components instead of putting all UI into `MainActivity.kt`.

Recommended separation:

```text
chat/
├── ChatModels.kt
├── ChatRepository.kt
├── ChatShell.kt
├── ChatHomeScreen.kt
├── ChatDetailScreen.kt
├── ChatAnimations.kt
└── ChatComponents.kt

workspace/
├── WorkspaceShell.kt
├── WorkspaceHomeScreen.kt
└── existing Workspace tools...
```

Animation implementation should use stable Compose animation primitives and avoid unnecessary recomposition. Use list-aware animation for message/list changes and IME-aware layout behavior for the composer.

## Logo rule

**SOURCE OF TRUTH:** the user-supplied 3D green/blue WAW logo in the reference image.

Do not silently substitute:

- purple shield WAW logo;
- generic WhatsApp icon;
- generic Material icon as the WAW brand mark.

If the exact binary logo is not yet present in the repository, implementation must keep a dedicated logo asset slot so the supplied asset can be inserted without redesigning the UI.

## Quality gate

This visual specification is complete only when:

- [ ] White visual system applied consistently.
- [ ] Supplied WAW logo is used when the binary asset is available.
- [ ] Top tabs scroll smoothly to Workspace.
- [ ] Home/chat list matches the reference structure.
- [ ] Chat detail matches the reference structure.
- [ ] Incoming message animation works.
- [ ] Outgoing message animation works.
- [ ] Typing animation works.
- [ ] Message status transitions work.
- [ ] New-message badge animation works.
- [ ] Attachment progress animation works.
- [ ] Composer follows keyboard smoothly.
- [ ] Voice waveform animation works.
- [ ] Workspace Quick Access is integrated.
- [ ] Existing W1/W2 Workspace functions remain available.
- [ ] Android build passes.
- [ ] Unit tests pass.
- [ ] Real-device smoke test passes before LOCK.

## Lock rule

**Do not mark this UI stage LOCKED just because code compiles.**

Required sequence:

`DESIGN → IMPLEMENT → BUILD → TEST → FIX → RETEST → PASS → LOCK`

Real-device validation is still required before declaring the stage PASS/LOCK.

## Design intent

The target is a **real WAW production UI implementation**, not a static mockup or demo screen. The reference image defines the visual direction; behavior, state transitions, accessibility, persistence, and existing Workspace boundaries must remain real and testable.
