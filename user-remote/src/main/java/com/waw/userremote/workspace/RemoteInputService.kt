package com.waw.userremote.workspace

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class RemoteInputService : AccessibilityService() {
    override fun onServiceConnected() { instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    private fun handle(raw: String) {
        val msg = runCatching { JSONObject(raw) }.getOrNull() ?: return
        if (!active || msg.optString("capability") !in setOf("TOUCH_INPUT", "KEYBOARD_INPUT")) return
        when (msg.optString("inputType")) {
            "TOUCH_DOWN", "TOUCH_MOVE", "TOUCH_UP" -> {
                if (!msg.has("x") || !msg.has("y")) return
                val x = msg.optDouble("x", -1.0).toFloat(); val y = msg.optDouble("y", -1.0).toFloat()
                if (x < 0 || y < 0 || x > 10000 || y > 10000) return
                dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(x, y) }, 0, if (msg.optString("inputType") == "TOUCH_UP") 1 else 80)).build(), null, null)
            }
            "KEY_DOWN" -> when (msg.optInt("keyCode", -1)) { 3 -> performGlobalAction(GLOBAL_ACTION_HOME); 4 -> performGlobalAction(GLOBAL_ACTION_BACK) }
        }
    }
    companion object {
        @Volatile private var instance: RemoteInputService? = null
        @Volatile private var active = false
        fun activate(value: Boolean) { active = value }
        fun dispatch(raw: String) { instance?.handle(raw) }
    }
}
