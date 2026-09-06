# Remote transport contract

The remote peer must establish a mutually authenticated encrypted session before accepting `ScreenFrame` or `InputCommand` messages. Pair requests use a six-digit one-time code and expire after a short window. The Android host must approve MediaProjection before sending frames.

## Direction

- Android host to Windows viewer: send `ScreenFrame`; Windows sends approved `InputCommand` only when the host has enabled control.
- Android host to Android viewer: same contract, with Android Accessibility/Input permission required before touch injection.

## Required controls

The peer must display the host device name, capabilities, session timer, and a visible disconnect action. A disconnect message invalidates the session immediately. No WhatsApp cookies, credentials, contacts, or chat contents are included in this protocol.

`RemoteProtocol.kt` defines the shared message model. A production transport still needs a TLS/WebSocket implementation and a Windows agent with code signing.

## Local discovery

`RemoteDiscovery.kt` advertises and discovers `_wawremote._tcp` peers on the same Wi-Fi network. Discovery only shows candidate devices; it never authenticates, starts screen sharing, or grants input access. The one-time pairing code and host approval remain mandatory.
