package com.parksandcafe.app.maps

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.parksandcafe.app.QuickSaveActivity

/**
 * Watches for Google Maps coming to the foreground, then offers the quick-save UI.
 *
 * Play Store: this is a restricted Accessibility use. Declare it in Play Console
 * (Sensitive permissions → Accessibility) with a clear justification: detect Maps
 * only to show Park/Cafe save buttons. Do not expand event types or package scope
 * without revisiting policy.
 */
class MapsDetectAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg != MAPS_PACKAGE) return

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_MAPS_SAVE_ENABLED, true)) return

        val now = System.currentTimeMillis()
        val last = prefs.getLong(KEY_LAST_PROMPT_MS, 0L)
        if (now - last < DEBOUNCE_MS) return
        prefs.edit().putLong(KEY_LAST_PROMPT_MS, now).apply()

        val launch = Intent(this, QuickSaveActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(QuickSaveActivity.EXTRA_SOURCE, QuickSaveActivity.SOURCE_MAPS)
        }
        startActivity(launch)
    }

    override fun onInterrupt() = Unit

    companion object {
        const val MAPS_PACKAGE = "com.google.android.apps.maps"
        const val PREFS = "parks_cafe_maps"
        const val KEY_MAPS_SAVE_ENABLED = "maps_save_enabled"
        const val KEY_LAST_PROMPT_MS = "last_prompt_ms"
        private const val DEBOUNCE_MS = 45_000L
    }
}
