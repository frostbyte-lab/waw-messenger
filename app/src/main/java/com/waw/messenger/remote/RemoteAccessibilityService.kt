package com.waw.messenger.remote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class RemoteAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun dispatchCommand(raw: String) {
        val message = runCatching { JSONObject(raw) }.getOrNull() ?: return
        when (message.optString("inputType")) {
            "TOUCH_DOWN", "TOUCH_MOVE", "TOUCH_UP" -> {
                val x = message.optDouble("x", -1.0).toFloat()
                val y = message.optDouble("y", -1.0).toFloat()
                if (x < 0 || y < 0) return
                val path = Path().apply { moveTo(x, y) }
                val stroke = GestureDescription.StrokeDescription(path, 0, if (message.optString("inputType") == "TOUCH_UP") 1 else 80)
                dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
            }
            "KEY_DOWN" -> when (message.optInt("keyCode", -1)) {
                4 -> performGlobalAction(GLOBAL_ACTION_BACK)
                3 -> performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    companion object {
        @Volatile private var instance: RemoteAccessibilityService? = null
        fun dispatch(raw: String) { instance?.dispatchCommand(raw) }
    }
}
