package com.waw.messenger.remote

import java.util.UUID

/** Wire-level message types shared by Android and future Windows peer. */
sealed interface RemoteMessage {
    val sessionId: String
}

data class PairRequest(
    override val sessionId: String = UUID.randomUUID().toString(),
    val oneTimeCode: String,
    val deviceName: String,
    val target: RemoteTarget,
    val expiresAtEpochMs: Long
) : RemoteMessage

data class PairAccepted(
    override val sessionId: String,
    val hostDeviceName: String,
    val capabilities: Set<RemoteCapability>
) : RemoteMessage

data class ScreenFrame(
    override val sessionId: String,
    val sequence: Long,
    val width: Int,
    val height: Int,
    val encodedPayload: ByteArray
) : RemoteMessage

data class InputCommand(
    override val sessionId: String,
    val type: InputType,
    val x: Float? = null,
    val y: Float? = null,
    val keyCode: Int? = null
) : RemoteMessage

enum class RemoteTarget { WINDOWS, ANDROID }
enum class RemoteCapability { SCREEN_SHARE, TOUCH_INPUT, KEYBOARD_INPUT }

enum class InputType { TOUCH_DOWN, TOUCH_MOVE, TOUCH_UP, KEY_DOWN, KEY_UP, DISCONNECT }
