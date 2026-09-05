package com.waw.messenger

import com.waw.messenger.linked.LinkedDeviceWebViewActivity

/**
 * Launcher compatibility name retained for existing intents and tests.
 * Track A is the official WhatsApp Web linked viewer; legacy auth/chat is not used.
 */
class MainActivity : LinkedDeviceWebViewActivity()
