#!/usr/bin/env bash
set -euo pipefail
manifest='app/src/main/AndroidManifest.xml'
workspace='app/src/main/java/com/waw/messenger/WorkspaceActivity.kt'
linked='app/src/main/java/com/waw/messenger/linked/LinkedDeviceWebViewActivity.kt'

launcher=$(awk '/<activity android:name="\.WorkspaceActivity"/{found=1} found && /MAIN/{main=1} found && /LAUNCHER/{launcher=1} found && /<\/activity>/{exit !(main && launcher)}' "$manifest" && echo yes || true)
test "$launcher" = yes || { echo 'FAIL: WorkspaceActivity is not the sole Hybrid launcher'; exit 1; }
! grep -qE 'web\.whatsapp\.com|loadUrl\(' "$workspace" || { echo 'FAIL: launcher contains direct WhatsApp Web loading'; exit 1; }
grep -q 'addBusinessConsentGate()' "$linked" || { echo 'FAIL: Business WebView has no explicit consent gate'; exit 1; }
if sed -n '70,105p' "$linked" | grep -q 'loadOfficialWhatsApp'; then echo 'FAIL: WhatsApp loads during onCreate'; exit 1; fi
echo 'PASS: WAW Hybrid launcher and explicit Business boundary'
