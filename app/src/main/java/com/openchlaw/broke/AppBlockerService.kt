package com.openchlaw.broke

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences

class AppBlockerService : AccessibilityService() {

    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences("BrokePrefs", Context.MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        // 1. Check if Blocking is Active
        val isBlocking = prefs.getBoolean("is_blocking", false)
        if (!isBlocking) return

        // 2. Get the package name of the app being opened
        val packageName = event.packageName?.toString() ?: return

        // 3. Ignore our own app and system apps (basic filter)
        if (packageName == this.packageName) return
        
        // 4. Check if the app is in the Block List
        val blockedApps = prefs.getStringSet("blocked_packages", emptySet()) ?: emptySet()
        
        if (blockedApps.contains(packageName)) {
            // 5. BLOCK IT!
            // Start the BlockActivity immediately
            val intent = Intent(this, BlockActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("blocked_package", packageName)
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // Required method
    }
}
